package org.golem.raknet.handler.connected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import org.golem.raknet.Server
import org.golem.raknet.enums.Flags
import org.golem.raknet.handler.ConnectionException
import org.golem.raknet.handler.MessageEnvelope
import org.golem.raknet.message.*
import org.golem.raknet.message.datagram.Datagram

class ConnectedMessageDecoder(private val server: Server): MessageToMessageDecoder<DatagramPacket>() {

    override fun decode(ctx: ChannelHandlerContext, msg: DatagramPacket, output: MutableList<Any>) {
        // Ignore the packet if the sender isn't connected or if the packet is too small
        if(!server.hasConnection(msg.sender()) || msg.content().readableBytes() < 1) return
        val buffer = msg.content()
        val id: Int = buffer.readUnsignedByte().toInt()
        val decoded: OnlineMessage = when(MessageType.find(id)) {
            MessageType.ACK -> Acknowledge.from(buffer)
            MessageType.NACK -> NAcknowledge.from(buffer)
            else -> {
                if(id and Flags.DATAGRAM.id() == 0) {
                    // A datagram wasn't received
                    println("Received a datagram packet, but it wasn't a datagram. Ignoring...")
                    throw ConnectionException("Received a message that wasn't a datagram while connected")
                }
                // Reset reader index to the beginning of the buffer so that the datagram can decode its own flags
                buffer.resetReaderIndex()
                Datagram.from(buffer)
            }
        }
        output.add(MessageEnvelope(decoded, msg.sender()))
    }
}