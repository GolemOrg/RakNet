package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.ConnectedPacket
import raknet.packet.MessageType

class ConnectedPong(
    var pingTime: Long,
    var pongTime: Long
): ConnectedPacket(MessageType.CONNECTED_PONG.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(pingTime, pongTime)

    companion object {
        fun from(buffer: ByteBuf) = ConnectedPong(buffer.readLong(), buffer.readLong())
    }

    override fun toString()  = "ConnectedPong(pingTime=$pingTime, pongTime=$pongTime)"
}