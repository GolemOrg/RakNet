package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.DataPacket
import raknet.packet.PacketType

class ConnectedPing(
    val time: Long
): DataPacket(PacketType.CONNECTED_PING.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(time)
    }

    companion object {
        fun from(buffer: ByteBuf): ConnectedPing {
            return ConnectedPing(
                buffer.readLong()
            )
        }
    }

    override fun toString(): String {
        return "ConnectedPingPacket(time=$time)"
    }
}