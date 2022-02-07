package raknet.handler.connected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import raknet.Server
import raknet.handler.PacketEnvelope
import raknet.packet.*

class ConnectedMessageDecoder(private val server: Server): MessageToMessageDecoder<DatagramPacket>() {

    override fun decode(ctx: ChannelHandlerContext, msg: DatagramPacket, output: MutableList<Any>) {
        // Ignore the packet if the sender isn't connected or if the packet is too small
        if(!server.hasConnection(msg.sender()) || msg.content().readableBytes() < 1) return
        val buffer = msg.content()
        val id: Int = buffer.readUnsignedByte().toInt()
        val decoded: ConnectedPacket? = when(MessageType.find(id)) {
            MessageType.ACK -> null // TODO: Acknowledges :(
            MessageType.NACK -> null // TODO: NAcknowledges :(
            else -> null // TODO: Datagrams :(
        }
        if(decoded != null) output.add(PacketEnvelope(decoded, msg.sender()))
    }
}