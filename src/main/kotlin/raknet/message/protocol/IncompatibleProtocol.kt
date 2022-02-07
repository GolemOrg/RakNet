package raknet.message.protocol

import io.netty.buffer.ByteBuf
import raknet.message.MessageType
import raknet.message.OfflineMessage
import raknet.types.Magic
import raknet.types.Magic.readMagic

class IncompatibleProtocol(
    var protocol: Byte,
    var magic: Magic,
    var serverGuid: Long,
): OfflineMessage(MessageType.INCOMPATIBLE_PROTOCOL_VERSION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(protocol, magic, serverGuid)

    companion object {
        fun from(buffer: ByteBuf) = IncompatibleProtocol(buffer.readByte(), buffer.readMagic(), buffer.readLong())
    }

    override fun toString() = "IncompatibleProtocol(protocol=$protocol, serverGuid=$serverGuid)"
}