package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.DataPacket
import raknet.packet.PacketType
import raknet.readString

class UnconnectedPong(
    var pingId: Long,
    var guid: Long,
    var magic: Magic,
    var serverName: String,
): DataPacket(PacketType.UNCONNECTED_PONG.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(pingId, guid, magic, serverName)

    companion object {
        fun from(buffer: ByteBuf): UnconnectedPong = UnconnectedPong(
            buffer.readLong(),
            buffer.readLong(),
            buffer.readMagic(),
            buffer.readString()
        )
    }

    override fun toString(): String = "UnconnectedPongPacket(pingId=$pingId, guid=$guid, serverName='$serverName')"

}