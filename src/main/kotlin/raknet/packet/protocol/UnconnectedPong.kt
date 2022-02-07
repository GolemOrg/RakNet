package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.types.Magic
import raknet.types.Magic.readMagic
import raknet.packet.OfflinePacket
import raknet.packet.MessageType
import raknet.readString

class UnconnectedPong(
    var pingId: Long,
    var guid: Long,
    var magic: Magic,
    var serverName: String,
): OfflinePacket(MessageType.UNCONNECTED_PONG.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(pingId, guid, magic, serverName)

    companion object {
        fun from(buffer: ByteBuf): UnconnectedPong = UnconnectedPong(
            buffer.readLong(),
            buffer.readLong(),
            buffer.readMagic(),
            buffer.readString()
        )
    }

    override fun toString() = "UnconnectedPong(pingId=$pingId, guid=$guid, serverName='$serverName')"

}