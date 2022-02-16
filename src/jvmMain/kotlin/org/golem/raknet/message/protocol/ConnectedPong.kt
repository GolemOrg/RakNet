package org.golem.raknet.message.protocol

import io.netty.buffer.ByteBuf
import org.golem.raknet.message.OnlineMessage
import org.golem.raknet.message.MessageType

class ConnectedPong(
    var pingTime: Long,
    var pongTime: Long
): OnlineMessage(MessageType.CONNECTED_PONG.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(pingTime, pongTime)

    companion object {
        fun from(buffer: ByteBuf) = ConnectedPong(buffer.readLong(), buffer.readLong())
    }

    override fun toString()  = "ConnectedPong(pingTime=$pingTime, pongTime=$pongTime)"
}