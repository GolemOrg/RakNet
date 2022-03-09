package org.golem.raknet.connection

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import org.golem.raknet.enums.Flags
import org.golem.raknet.handler.MessageEnvelope
import org.golem.raknet.message.*
import org.golem.raknet.message.datagram.*
import org.golem.raknet.message.protocol.*
import org.golem.netty.split
import org.golem.netty.types.UMediumLE

class InternalsHandler(
    private val connection: Connection,
    private val context: ChannelHandlerContext
) {

    private val ackQueue = mutableListOf<UInt>()
    private val nackQueue = mutableListOf<UInt>()

    var lastReceivedMessageTime = System.currentTimeMillis()
    var largestReceivedDatagramNumber: UInt = 0u

    var currentDatagramSequenceNumber: UInt = 0u
    var currentMessageReliableIndex: UInt = 0u
    var currentOrderIndex: UInt = 0u
    private val currentFragmentIndex: Short = 0

    private val sendQueue: MutableList<Frame> = mutableListOf()
    private val resendQueue: MutableMap<UMediumLE, Frame> = mutableMapOf()
    private val pendingFrames: MutableMap<Short, FrameBuilder> = mutableMapOf()

    fun tick() {
        if(connection.state === ConnectionState.DISCONNECTED) {
            return
        }

        if(ackQueue.size > 0) dispatchAckQueue()
        if(nackQueue.size > 0) dispatchNAckQueue()
        if(sendQueue.size > 0) dispatchFrameQueue()

        if(System.currentTimeMillis() - lastReceivedMessageTime > TimeComponent.TIMEOUT.toLong()) {
            connection.close(DisconnectionReason.Timeout)
        }
    }

    private fun createDefaultFrame(buffer: ByteBuf, fragment: Fragment? = null): Frame {
        return Frame.ReliableOrdered(
            messageIndex = UMediumLE(currentMessageReliableIndex++),
            order = Order(
                index = UMediumLE(currentOrderIndex++),
                channel = 0
            ),
            fragment = fragment,
            body = buffer
        )
    }

    fun send(packet: OnlineMessage, immediate: Boolean = false) {
        if(!connection.isConnected) {
            return
        }
        val buffer = packet.prepare()
        if(buffer.readableBytes() > connection.mtuSize) {
            val buffers = buffer.split(connection.mtuSize.toInt())
            buffers.forEachIndexed { index, current ->
                val frame = createDefaultFrame(
                    buffer = current,
                    fragment = Fragment(
                        count = buffers.size,
                        fragmentId = currentFragmentIndex,
                        index = index
                    )
                )
                addFrameToQueue(frame)
            }
            buffer.release()
        } else {
            val frame = createDefaultFrame(buffer)
            addFrameToQueue(frame)
        }
        // TODO: Priority system rather than immediate/non-immediate
        if(immediate) {
            dispatchFrameQueue()
        }
    }

    fun sendInternal(packet: OnlineMessage) {
        val prepared = packet.prepare()
        try {
            val datagram = Datagram(
                flags = mutableListOf(Flags.DATAGRAM),
                datagramSequenceNumber = UMediumLE(currentDatagramSequenceNumber++),
                frames = mutableListOf(
                    Frame.Unreliable(
                        body = prepared,
                        fragment = null
                    )
                )
            )
            context.writeAndFlush(MessageEnvelope(datagram, connection.address))
        } finally {
            prepared.release()
        }
    }

    /**
     * TODO: Hack! We need to find a better solution
     *
     * The best way to do this would be to use a method that
     * can release the buffers inside after the write is done?
     *
     * If possible, it'd be good to use the packet envelope
     * for sending the datagram, but seeing as the datagram
     * will still hold frames w/ buffers inside of them,
     * that is not possible in our current system.
     */
    fun write(datagram: Datagram) {
        val prepared = datagram.prepare()
        context.writeAndFlush(DatagramPacket(prepared, connection.address))
        for(frame in datagram.frames) {
            frame.body.release()
        }
    }

    fun write(acknowledge: Acknowledge) {
        context.writeAndFlush(MessageEnvelope(acknowledge, connection.address))
    }

    fun write(nacknowledge: NAcknowledge) {
        context.writeAndFlush(MessageEnvelope(nacknowledge, connection.address))
    }

    private fun dispatchAckQueue() {
        val acknowledge = Acknowledge.fromQueue(ackQueue)
        write(acknowledge)
        ackQueue.clear()
    }

    private fun dispatchNAckQueue() {
        write(NAcknowledge.fromQueue(nackQueue))
        nackQueue.clear()
    }

    private fun addFrameToQueue(frame: Frame) {
        when(frame) {
            is Frame.Reliable -> {
                resendQueue[frame.messageIndex] = frame
            }
            is Frame.ReliableOrdered -> {
                resendQueue[frame.messageIndex] = frame
                // Ensure ordering
            }
            else -> {}
        }
        sendQueue.add(frame)
    }

    private fun dispatchFrameQueue() {
        val datagram = Datagram(
            flags = mutableListOf(Flags.DATAGRAM),
            datagramSequenceNumber = UMediumLE(currentDatagramSequenceNumber++),
            frames = sendQueue
        )
        write(datagram)
        sendQueue.clear()
    }

    fun handle(datagram: Datagram) {
        largestReceivedDatagramNumber = maxOf(datagram.datagramSequenceNumber.toUInt(), largestReceivedDatagramNumber)
        ackQueue.add(datagram.datagramSequenceNumber.toUInt())
        if(nackQueue.contains(datagram.datagramSequenceNumber.toUInt())) {
            nackQueue.remove(datagram.datagramSequenceNumber.toUInt())
        }
        // TODO: NAKs
        datagram.frames.forEach { frame ->
            val message = createMessage(frame)
            if(message != null && connection.state != ConnectionState.DISCONNECTED) {
                connection.handle(message)
            }
        }
    }

    fun handle(acknowledge: Acknowledge) {
        // Release any packets that are not needed anymore
        acknowledge.records.forEach { record ->
            record.asList().forEach { index -> resendQueue.remove(index) }
        }
    }

    fun handle(nAcknowledge: NAcknowledge) {
        // Queue up any packets that need to be resent
        nAcknowledge.records.forEach { record ->
            record.asList().forEach { index ->
                val frame = resendQueue[index] ?: throw IllegalStateException("Frame not found in resend queue")
                sendQueue.add(frame)
            }
        }
    }

    private fun createMessage(frame: Frame): OnlineMessage? {
        // If there is a fragment, we'll try to reconstruct the buffer with a builder
        // Otherwise, we'll just use the frame's buffer
        val buffer: ByteBuf = if(frame.fragment != null) {
            val fragment = frame.fragment
            // Create/get the frame builder from the fragment id
            // TODO: Track the builder's lifetime and clean it up if inactive
            val builder = pendingFrames.getOrPut(fragment.fragmentId) { FrameBuilder(fragment.count) }
            // Add the fragment to the builder
            builder.add(frame)
            // Release the buffer to avoid a leak
            frame.body.release()
            // If the builder isn't complete, return null from the entire function
            if(!builder.complete()) return null

            // Remove the builder from the pending frames & build it
            pendingFrames.remove(fragment.fragmentId)
            builder.build()
        } else { frame.body }

        // We have a message!
        try {
            val id = buffer.readUnsignedByte().toInt()
            return when(MessageType.find(id)) {
                MessageType.CONNECTED_PING -> ConnectedPing.from(buffer)
                MessageType.CONNECTED_PONG -> ConnectedPong.from(buffer)
                MessageType.CONNECTION_REQUEST -> ConnectionRequest.from(buffer)
                MessageType.DISCONNECTION_NOTIFICATION -> DisconnectionNotification()
                MessageType.NEW_INCOMING_CONNECTION -> NewIncomingConnection.from(buffer)
                else -> UserMessage(id, buffer.readBytes(buffer.readableBytes()))
            }
        } finally {
            //  Make sure to release the buffer after returning
            // TODO: Auto-releasing buffers?
            buffer.release()
        }
    }
}