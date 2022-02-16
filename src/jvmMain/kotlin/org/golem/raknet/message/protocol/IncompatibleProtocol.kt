package org.golem.raknet.message.protocol

import io.netty.buffer.ByteBuf
import org.golem.raknet.message.MessageType
import org.golem.raknet.message.OfflineMessage
import org.golem.raknet.types.Magic
import org.golem.raknet.types.Magic.readMagic

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