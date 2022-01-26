package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.packet.DataPacket
import raknet.packet.PacketType
import raknet.readMagic

class UnconnectedPongPacket constructor(
    var pingId: Long,
    var guid: Long,
    var magic: Magic = Magic,
    var serverName: String
    ): DataPacket(PacketType.UNCONNECTED_PONG.id()) {

    override fun decode(buffer: ByteBuf) {

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