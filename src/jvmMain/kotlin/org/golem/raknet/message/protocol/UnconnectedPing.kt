package org.golem.raknet.message.protocol

import io.netty.buffer.ByteBuf
import org.golem.raknet.types.Magic
import org.golem.raknet.types.Magic.readMagic
import org.golem.raknet.message.OfflineMessage
import org.golem.raknet.message.MessageType

class UnconnectedPing(
    var time: Long,
    var magic: Magic,
    var clientGuid: Long
): OfflineMessage(MessageType.UNCONNECTED_PING.id()) {

    override fun encodeOrder(): Array<Any> =  arrayOf(time, magic, clientGuid)

    companion object {
        fun from(buffer: ByteBuf) = UnconnectedPing(
            buffer.readLong(),
            buffer.readMagic(),
            buffer.readLong()
        )
    }

    override fun toString() = "UnconnectedPing(time=$time, clientGuid=$clientGuid)"
}