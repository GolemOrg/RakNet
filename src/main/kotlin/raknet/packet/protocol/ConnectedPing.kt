package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.ConnectedPacket
import raknet.packet.PacketType

class ConnectedPing(
    val time: Long
): ConnectedPacket(PacketType.CONNECTED_PING.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(time)

    companion object {
        fun from(buffer: ByteBuf) = ConnectedPing(buffer.readLong())
    }

    override fun toString() = "ConnectedPingPacket(time=$time)"
}