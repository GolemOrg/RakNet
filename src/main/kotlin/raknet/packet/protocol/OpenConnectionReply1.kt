package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.OfflinePacket
import raknet.packet.MessageType

class OpenConnectionReply1(
    var magic: Magic,
    var serverGuid: Long,
    var useSecurity: Boolean,
    var mtuSize: Short
): OfflinePacket(MessageType.OPEN_CONNECTION_REPLY_1.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(magic, serverGuid, useSecurity, mtuSize)

    companion object {
        fun from(buffer: ByteBuf) = OpenConnectionReply1(
            buffer.readMagic(),
            buffer.readLong(),
            buffer.readBoolean(),
            buffer.readShort()
        )
    }

    override fun toString() = "OpenConnectionReply1(magic=$magic, serverGuid=$serverGuid, useSecurity=$useSecurity, mtuSize=$mtuSize)"
}