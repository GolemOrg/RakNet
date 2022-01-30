package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.DataPacket
import raknet.packet.PacketType

class ConnectionRequest(
    var guid: Long,
    var time: Long,
): DataPacket(PacketType.CONNECTION_REQUEST.id()) {
    override fun encodeOrder(): Array<Any> {
        return arrayOf(guid, time)
    }

    companion object {
        fun from(buffer: ByteBuf): ConnectionRequest {
            return ConnectionRequest(
                buffer.readLong(),
                buffer.readLong()
            )
        }
    }

    override fun toString(): String {
        return "ConnectionRequestPacket(guid=$guid, time=$time)"
    }
}