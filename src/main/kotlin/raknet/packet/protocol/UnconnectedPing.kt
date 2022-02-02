package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.DataPacket
import raknet.packet.PacketType

class UnconnectedPing(
    var time: Long,
    var magic: Magic,
    var clientGuid: Long
): DataPacket(PacketType.UNCONNECTED_PING.id()) {

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