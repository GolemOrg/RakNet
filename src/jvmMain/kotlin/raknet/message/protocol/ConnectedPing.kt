package raknet.message.protocol

import io.netty.buffer.ByteBuf
import raknet.message.OnlineMessage
import raknet.message.MessageType

class ConnectedPing(
    val time: Long
): OnlineMessage(MessageType.CONNECTED_PING.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(time)

    companion object {
        fun from(buffer: ByteBuf) = ConnectedPing(buffer.readLong())
    }

    override fun toString() = "ConnectedPing(time=$time)"
}