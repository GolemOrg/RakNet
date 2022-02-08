package raknet.connection

import io.netty.channel.ChannelHandlerContext
import raknet.enums.Flags
import raknet.handler.MessageEnvelope
import raknet.message.Acknowledge
import raknet.message.MessageType
import raknet.message.NAcknowledge
import raknet.message.OnlineMessage
import raknet.message.datagram.Datagram
import raknet.message.datagram.Frame
import raknet.message.datagram.Order
import raknet.message.protocol.ConnectedPing
import raknet.message.protocol.ConnectionRequest
import raknet.message.protocol.DisconnectionNotification
import raknet.message.protocol.NewIncomingConnection
import raknet.types.UInt24LE

class InternalsHandler(
    val connection: Connection,
    val context: ChannelHandlerContext
) {

    val ackQueue = mutableListOf<Acknowledge>()
    val nakQueue = mutableListOf<NAcknowledge>()

    var currentDatagramSequenceNumber: UInt = 0u
    var currentMessageReliableIndex: UInt = 0u
    var currentOrderIndex: UInt = 0u

    val frameQueue = mutableListOf<Frame>()

    fun tick() {

        if(frameQueue.size > 0) {
            sendFrames()
        }
    }

    fun send(packet: OnlineMessage, immediate: Boolean = false) {
        frameQueue.add(Frame.ReliableOrdered(
            messageIndex = UInt24LE(currentMessageReliableIndex++),
            order = Order(
                index = UInt24LE(currentOrderIndex++),
                channel = 0
            ),
            fragment = null,
            body = packet.prepare()
        ))

        if(immediate) {
            sendFrames()
        }
    }

    fun write(datagram: Datagram) = context.writeAndFlush(MessageEnvelope(datagram, connection.address))

    fun sendFrames() {
        val datagram = Datagram(
            flags = mutableListOf(Flags.DATAGRAM),
            datagramSequenceNumber = UInt24LE(currentDatagramSequenceNumber++),
            frames = frameQueue
        )
        write(datagram)
        frameQueue.clear()
    }

    fun handle(datagram: Datagram) {
        for(frame in datagram.frames) {
            val body = frame.body
            val message: OnlineMessage? = when(MessageType.find(body.readUnsignedByte().toInt())) {
                MessageType.CONNECTED_PING -> ConnectedPing.from(body)
                MessageType.CONNECTION_REQUEST -> ConnectionRequest.from(body)
                MessageType.DISCONNECTION_NOTIFICATION -> DisconnectionNotification()
                MessageType.NEW_INCOMING_CONNECTION -> NewIncomingConnection.from(body)
                else -> null // UserMessage (pass to user)
            }
            body.release()
            if(message != null) {
                connection.handle(message)
            }
        }
    }

    fun handle(acknowledge: Acknowledge) {

    }

    fun handle(nAcknowledge: NAcknowledge) {

    }
}