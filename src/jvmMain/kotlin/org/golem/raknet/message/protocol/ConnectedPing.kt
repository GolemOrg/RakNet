package org.golem.raknet.message.protocol

import io.netty.buffer.ByteBuf
import org.golem.raknet.message.OnlineMessage
import org.golem.raknet.message.MessageType

class ConnectedPing(
    val time: Long
): OnlineMessage(MessageType.CONNECTED_PING.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(time)

    companion object {
        fun from(buffer: ByteBuf) = ConnectedPing(buffer.readLong())
    }

    override fun toString() = "ConnectedPing(time=$time)"
}