package org.golem.raknet.message.protocol

import io.netty.buffer.ByteBuf
import org.golem.raknet.types.Magic
import org.golem.raknet.types.Magic.readMagic
import org.golem.raknet.message.OfflineMessage
import org.golem.raknet.message.MessageType

class OpenConnectionReply1(
    var magic: Magic,
    var serverGuid: Long,
    var useSecurity: Boolean,
    var mtuSize: Short
): OfflineMessage(MessageType.OPEN_CONNECTION_REPLY_1.id()) {

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