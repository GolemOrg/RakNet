package raknet.connection

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import raknet.enums.Flags
import raknet.handler.MessageEnvelope
import raknet.message.*
import raknet.message.datagram.*
import raknet.message.protocol.ConnectedPing
import raknet.message.protocol.ConnectionRequest
import raknet.message.protocol.DisconnectionNotification
import raknet.message.protocol.NewIncomingConnection
import raknet.split
import raknet.types.UInt24LE

class InternalsHandler(
    private val connection: Connection,
    private val context: ChannelHandlerContext
) {

    private val ackQueue = mutableListOf<UInt>()
    private val nackQueue = mutableListOf<UInt>()

    var lastReceivedMessageTime = System.currentTimeMillis()
    var lastReceivedDatagramSequenceNumber: UInt = 0u

    var currentDatagramSequenceNumber: UInt = 0u
    var currentMessageReliableIndex: UInt = 0u
    var currentOrderIndex: UInt = 0u
    private val currentFragmentIndex: Short = 0.toShort()

    private val sendQueue: MutableList<Frame> = mutableListOf()
    private val resendQueue: MutableMap<UInt24LE, Frame> = mutableMapOf()
    private val pendingFrames: MutableMap<Short, FrameBuilder> = mutableMapOf()

    fun tick() {
        if(ackQueue.size > 0) dispatchAckQueue()
        if(nackQueue.size > 0) dispatchNAckQueue()
        if(sendQueue.size > 0) dispatchFrameQueue()

        if(System.currentTimeMillis() - lastReceivedMessageTime > TimeComponent.TIMEOUT.toLong()) {
            connection.close(DisconnectionReason.Timeout)
        }
    }

    private fun createDefaultFrame(buffer: ByteBuf, fragment: Fragment? = null): Frame {
        return Frame.ReliableOrdered(
            messageIndex = UInt24LE(currentMessageReliableIndex++),
            order = Order(
                index = UInt24LE(currentOrderIndex++),
                channel = 0
            ),
            fragment = fragment,
            body = buffer
        )
    }

    fun send(packet: OnlineMessage, immediate: Boolean = false) {
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
                datagramSequenceNumber = UInt24LE(currentDatagramSequenceNumber++),
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
            datagramSequenceNumber = UInt24LE(currentDatagramSequenceNumber++),
            frames = sendQueue
        )
        write(datagram)
        sendQueue.clear()
    }

    fun handle(datagram: Datagram) {
        ackQueue.add(datagram.datagramSequenceNumber.toUInt())
        for(frame in datagram.frames) {
            val body = frame.body
            val message: OnlineMessage? = when(MessageType.find(body.readUnsignedByte().toInt())) {
                MessageType.CONNECTED_PING -> ConnectedPing.from(body)
                MessageType.CONNECTION_REQUEST -> ConnectionRequest.from(body)
                MessageType.DISCONNECTION_NOTIFICATION -> DisconnectionNotification()
                MessageType.NEW_INCOMING_CONNECTION -> NewIncomingConnection.from(body)
                else -> buildUserMessage(frame)
            }
            body.release()
            if(message != null) {
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
                resendQueue[index]?.let {
                    addFrameToQueue(it)
                }
            }
        }
    }

    private fun buildUserMessage(frame: Frame): UserMessage? {
        val body = frame.body
        if(frame.fragment != null) {
            val fragment = frame.fragment
            val builder = pendingFrames.getOrPut(fragment.fragmentId) { FrameBuilder(fragment.count) }
            builder.add(frame)
            if(builder.complete()) {
                val buffer = builder.build()
                pendingFrames.remove(fragment.fragmentId)
                return UserMessage(buffer.readUnsignedByte().toInt(), buffer)
            }
            return null
        }
        return UserMessage(body.readUnsignedByte().toInt(), body.readBytes(body.readableBytes()))
    }
}