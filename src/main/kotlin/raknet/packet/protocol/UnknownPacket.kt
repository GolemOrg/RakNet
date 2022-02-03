package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.ConnectedPacket

class UnknownPacket(
    override val id: Int,
    val buffer: ByteBuf
): ConnectedPacket(id) {

    override fun encodeOrder(): Array<Any> = arrayOf(buffer)

}