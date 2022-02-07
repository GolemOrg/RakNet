package raknet.handler.connected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import raknet.Server
import raknet.enums.Flags
import raknet.handler.MessageEnvelope
import raknet.message.*
import raknet.message.datagram.Datagram

class ConnectedMessageDecoder(private val server: Server): MessageToMessageDecoder<DatagramPacket>() {

    override fun decode(ctx: ChannelHandlerContext, msg: DatagramPacket, output: MutableList<Any>) {
        // Ignore the packet if the sender isn't connected or if the packet is too small
        if(!server.hasConnection(msg.sender()) || msg.content().readableBytes() < 1) return
        val buffer = msg.content()
        val id: Int = buffer.readUnsignedByte().toInt()
        val decoded: OnlineMessage? = when(MessageType.find(id)) {
            MessageType.ACK -> null // Acknowledge.from(buffer)
            MessageType.NACK -> null // NAcknowledge.from(buffer)
            else -> {
                if(id and Flags.DATAGRAM.id() == 0) {
                    // A datagram wasn't received
                    return
                }
                Datagram.from(buffer)
            }
        }
        if(decoded != null) output.add(MessageEnvelope(decoded, msg.sender()))
    }
}