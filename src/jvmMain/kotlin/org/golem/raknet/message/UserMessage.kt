package org.golem.raknet.message

import io.netty.buffer.ByteBuf

class UserMessage(id: Int, val buffer: ByteBuf): OnlineMessage(id) {
    override fun encodeOrder(): Array<Any> = arrayOf(id, buffer)
    override fun toString(): String = "UserMessage(id=$id, buffer=ByteBuf(${buffer.readableBytes()}))"

}