package raknet.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageEncoder
import raknet.packet.DataPacket

class MessageEncoder<T: DataPacket>: MessageToMessageEncoder<PacketEnvelope<T>>() {

    override fun encode(ctx: ChannelHandlerContext, msg: PacketEnvelope<T>, out: MutableList<Any>) {
        ctx.writeAndFlush(DatagramPacket(msg.content().prepare(), msg.sender()))
    }
}