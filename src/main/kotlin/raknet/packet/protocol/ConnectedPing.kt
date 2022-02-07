package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.ConnectedPacket
import raknet.packet.MessageType

class ConnectedPing(
    val time: Long
): ConnectedPacket(MessageType.CONNECTED_PING.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(time)

    companion object {
        fun from(buffer: ByteBuf) = ConnectedPing(buffer.readLong())
    }

    override fun toString() = "ConnectedPing(time=$time)"
}