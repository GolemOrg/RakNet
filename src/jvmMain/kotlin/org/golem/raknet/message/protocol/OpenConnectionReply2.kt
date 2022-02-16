package org.golem.raknet.message.protocol

import io.netty.buffer.ByteBuf
import org.golem.raknet.types.Magic
import org.golem.raknet.types.Magic.readMagic
import org.golem.raknet.message.OfflineMessage
import org.golem.raknet.message.MessageType
import org.golem.raknet.readAddress
import java.net.InetSocketAddress

class OpenConnectionReply2(
    var magic: Magic,
    var serverGuid: Long,
    var clientAddress: InetSocketAddress,
    var mtuSize: Short,
    var encryptionEnabled: Boolean,
): OfflineMessage(MessageType.OPEN_CONNECTION_REPLY_2.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(magic, serverGuid, clientAddress, mtuSize, encryptionEnabled)

    companion object {
        fun from(buffer: ByteBuf) = OpenConnectionReply2(
            buffer.readMagic(),
            buffer.readLong(),
            buffer.readAddress(),
            buffer.readShort(),
            buffer.readBoolean()
        )
    }

    override fun toString() = "OpenConnectionReply2(magic=$magic, serverGuid=$serverGuid, clientAddress=$clientAddress, mtuSize=$mtuSize, encryptionEnabled=$encryptionEnabled)"
}