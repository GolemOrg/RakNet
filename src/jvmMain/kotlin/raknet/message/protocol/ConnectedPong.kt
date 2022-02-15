package raknet.message.protocol

import io.netty.buffer.ByteBuf
import raknet.message.OnlineMessage
import raknet.message.MessageType

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