package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.OnlineMessage
import raknet.packet.MessageType

class ConnectionRequest(
    var guid: Long,
    var time: Long,
): OnlineMessage(MessageType.CONNECTION_REQUEST.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(guid, time)

    companion object {
        fun from(buffer: ByteBuf) = ConnectionRequest(buffer.readLong(), buffer.readLong())
    }

    override fun toString() = "ConnectionRequest(guid=$guid, time=$time)"
}