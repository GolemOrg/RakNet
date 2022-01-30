package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.DataPacket
import raknet.packet.PacketType

class UnconnectedPong(
    var pingId: Long,
    var guid: Long,
    var magic: Magic,
    var serverName: String,
): DataPacket(PacketType.UNCONNECTED_PONG.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(pingId, guid, magic, serverName)
    }

    companion object {
        fun from(buffer: ByteBuf): UnconnectedPong {
            return UnconnectedPong(
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