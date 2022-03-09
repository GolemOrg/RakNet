package org.golem.raknet.message.protocol

import io.netty.buffer.ByteBuf
import org.golem.raknet.types.Magic
import org.golem.raknet.types.Magic.readMagic
import org.golem.raknet.message.OfflineMessage
import org.golem.raknet.message.MessageType
import org.golem.raknet.types.RakString
import org.golem.raknet.types.readRakString

class UnconnectedPong(
    var pingId: Long,
    var guid: Long,
    var magic: Magic,
    var serverName: String,
): OfflineMessage(MessageType.UNCONNECTED_PONG.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(pingId, guid, magic, RakString(serverName))

    companion object {
        fun from(buffer: ByteBuf): UnconnectedPong = UnconnectedPong(
            buffer.readLong(),
            buffer.readLong(),
            buffer.readMagic(),
            buffer.readRakString()
        )
    }

    override fun toString() = "UnconnectedPong(pingId=$pingId, guid=$guid, serverName='$serverName')"

}