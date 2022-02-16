package org.golem.raknet.handler.unconnected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import org.golem.raknet.handler.MessageEnvelope
import org.golem.raknet.message.MessageType
import org.golem.raknet.message.OfflineMessage
import org.golem.raknet.message.protocol.OpenConnectionRequest1
import org.golem.raknet.message.protocol.OpenConnectionRequest2
import org.golem.raknet.message.protocol.UnconnectedPing

class UnconnectedMessageDecoder: MessageToMessageDecoder<DatagramPacket>() {

    override fun decode(ctx: ChannelHandlerContext, msg: DatagramPacket, output: MutableList<Any>) {
        val buffer = msg.content()
        // We need a packet ID if we even want to handle this
        if(buffer.readableBytes() < 1) return
        val decoded: OfflineMessage = when(MessageType.find(buffer.readUnsignedByte().toInt())) {
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
        output.add(MessageEnvelope(decoded, msg.sender()))
    }

}