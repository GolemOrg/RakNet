package org.golem.raknet.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageEncoder
import org.golem.raknet.message.DataMessage

class MessageEncoder<T: DataMessage>: MessageToMessageEncoder<MessageEnvelope<T>>() {

    override fun encode(ctx: ChannelHandlerContext, msg: MessageEnvelope<T>, out: MutableList<Any>) {
        val buffer = msg.content().prepare()
        ctx.writeAndFlush(DatagramPacket(buffer, msg.sender()))
        runCatching { buffer.release() }
    }
}