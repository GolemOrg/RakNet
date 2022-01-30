package raknet.packet.protocol.unconnected

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.DataPacket
import raknet.packet.PacketType

class UnconnectedPongPacket(
    var pingId: Long,
    var guid: Long,
    var magic: Magic,
    var serverName: String,
): DataPacket(PacketType.UNCONNECTED_PONG.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(pingId, guid, magic, serverName)
    }

    companion object {
        fun from(buffer: ByteBuf): UnconnectedPongPacket {
            return UnconnectedPongPacket(
                buffer.readLong(),
                buffer.readLong(),
                buffer.readMagic(),
                buffer.readCharSequence(buffer.readShort().toInt(), Charsets.UTF_8).toString()
            )
        }
    }

    override fun toString(): String {
        return "UnconnectedPongPacket(pingId=$pingId, guid=$guid, serverName='$serverName')"
    }

}