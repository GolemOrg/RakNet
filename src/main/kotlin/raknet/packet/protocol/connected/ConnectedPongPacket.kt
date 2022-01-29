package raknet.packet.protocol.connected

import io.netty.buffer.ByteBuf
import raknet.packet.DataPacket
import raknet.packet.PacketType

class ConnectedPongPacket(
    var pingTime: Long,
    var pongTime: Long
): DataPacket(PacketType.CONNECTED_PONG.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(pingTime, pongTime)
    }

    companion object {
        fun from(buffer: ByteBuf): ConnectedPongPacket {
            return ConnectedPongPacket(
                buffer.readLong(),
                buffer.readLong()
            )
        }
    }

    override fun toString(): String {
        return "ConnectedPongPacket(pingTime=$pingTime, pongTime=$pongTime)"
    }
}