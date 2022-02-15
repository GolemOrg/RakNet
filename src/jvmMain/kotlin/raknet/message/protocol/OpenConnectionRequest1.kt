package raknet.message.protocol

import io.netty.buffer.ByteBuf
import raknet.types.Magic
import raknet.types.Magic.readMagic
import raknet.message.OfflineMessage
import raknet.message.MessageType

class OpenConnectionRequest1(
    var magic: Magic,
    var protocolVersion: Byte,
    var mtuSize: Short
): OfflineMessage(MessageType.OPEN_CONNECTION_REQUEST_1.id()) {

    override fun encodeOrder(): Array<Any> =  arrayOf(magic, protocolVersion, mtuSize)

    companion object {
        fun from(buffer: ByteBuf) = OpenConnectionRequest1(
            buffer.readMagic(),
            buffer.readByte(),
            buffer.readableBytes().toShort()
        )
    }

    override fun toString() = "OpenConnectionRequest1(magic=$magic, protcolVersion=$protocolVersion, mtuSize=$mtuSize)"
}