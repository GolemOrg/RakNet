package raknet.packet.protocol.connected

import io.netty.buffer.ByteBuf
import raknet.packet.DataPacket
import raknet.packet.PacketType

class ConnectedPingPacket(
    val time: Long
): DataPacket(PacketType.CONNECTED_PING.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(time)
    }

    companion object {
        fun from(buffer: ByteBuf): ConnectedPingPacket {
            return ConnectedPingPacket(
                buffer.readLong()
            )
        }
    }

    override fun toString(): String {
        return "ConnectedPingPacket(time=$time)"
    }
}