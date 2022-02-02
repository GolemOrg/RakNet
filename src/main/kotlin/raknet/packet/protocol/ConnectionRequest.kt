package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.ConnectedPacket
import raknet.packet.PacketType

class ConnectionRequest(
    var guid: Long,
    var time: Long,
): ConnectedPacket(PacketType.CONNECTION_REQUEST.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(guid, time)

    companion object {
        fun from(buffer: ByteBuf) = ConnectionRequest(buffer.readLong(), buffer.readLong())
    }

    override fun toString() = "ConnectionRequest(guid=$guid, time=$time)"
}