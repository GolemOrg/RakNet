package raknet.handler.connected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import raknet.Server
import raknet.enums.Flag
import raknet.handler.PacketEnvelope
import raknet.packet.*

class ConnectedMessageDecoder(private val server: Server): MessageToMessageDecoder<DatagramPacket>() {

    override fun decode(ctx: ChannelHandlerContext, msg: DatagramPacket, output: MutableList<Any>) {
        // Ignore the packet if the sender isn't connected or if the packet is too small
        if(!server.hasConnection(msg.sender()) || msg.content().readableBytes() < 1) return
        val buffer = msg.content()
        val id: Int = buffer.readUnsignedByte().toInt()
        val decoded: ConnectedPacket = when(PacketType.find(id)) {
            PacketType.ACK -> Acknowledge.from(buffer)
            PacketType.NACK -> NAcknowledge.from(buffer)
            else -> {
                val isDatagram = id and Flag.DATAGRAM.id() != 0
                // We should probably log this, but it'll be fine for now
                if(!isDatagram) println("Received a packet with an unknown id: $id")
                Datagram.from(buffer.resetReaderIndex())
            }
        }
        output.add(PacketEnvelope(decoded, msg.sender()))
    }
}