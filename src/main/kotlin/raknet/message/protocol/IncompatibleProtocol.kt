package raknet.message.protocol

import io.netty.buffer.ByteBuf
import raknet.message.OnlineMessage
import raknet.message.MessageType

class IncompatibleProtocol(
    var protocol: Int,
    var serverGuid: Long,
): OnlineMessage(MessageType.INCOMPATIBLE_PROTOCOL_VERSION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(protocol, serverGuid)

    companion object {
        fun from(buffer: ByteBuf) = IncompatibleProtocol(buffer.readInt(), buffer.readLong())
    }

    override fun toString() = "IncompatibleProtocol(protocol=$protocol, serverGuid=$serverGuid)"
}