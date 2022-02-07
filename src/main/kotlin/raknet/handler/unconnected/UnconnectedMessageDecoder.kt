package raknet.handler.unconnected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import raknet.handler.PacketEnvelope
import raknet.packet.MessageType
import raknet.packet.OfflinePacket
import raknet.packet.protocol.OpenConnectionRequest1
import raknet.packet.protocol.OpenConnectionRequest2
import raknet.packet.protocol.UnconnectedPing

class UnconnectedMessageDecoder: MessageToMessageDecoder<DatagramPacket>() {

    override fun decode(ctx: ChannelHandlerContext, msg: DatagramPacket, output: MutableList<Any>) {
        val buffer = msg.content()
        // We need a packet ID if we even want to handle this
        if(buffer.readableBytes() < 1) return
        val decoded: OfflinePacket = when(MessageType.find(buffer.readUnsignedByte().toInt())) {
            MessageType.UNCONNECTED_PING -> UnconnectedPing.from(buffer)
            MessageType.OPEN_CONNECTION_REQUEST_1 -> OpenConnectionRequest1.from(buffer)
            MessageType.OPEN_CONNECTION_REQUEST_2 -> OpenConnectionRequest2.from(buffer)
            else -> {
                // Reset the buffer reader index & push the message back to the pipeline
                buffer.resetReaderIndex()
                output.add(msg.retain())
                return
            }
        }
        output.add(PacketEnvelope(decoded, msg.sender()))
    }

}