package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.ConnectedPacket
import raknet.packet.PacketType

class ConnectedPong(
    var pingTime: Long,
    var pongTime: Long
): ConnectedPacket(PacketType.CONNECTED_PONG.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(pingTime, pongTime)

    companion object {
        fun from(buffer: ByteBuf) = ConnectedPong(buffer.readLong(), buffer.readLong())
    }

    override fun toString()  = "ConnectedPong(pingTime=$pingTime, pongTime=$pongTime)"
}