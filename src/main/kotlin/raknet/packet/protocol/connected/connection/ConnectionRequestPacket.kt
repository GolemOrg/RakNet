package raknet.packet.protocol.connected.connection

import io.netty.buffer.ByteBuf
import raknet.packet.DataPacket
import raknet.packet.PacketType

class ConnectionRequestPacket(
    var guid: Long,
    var time: Long,
): DataPacket(PacketType.CONNECTION_REQUEST.id()) {
    override fun encodeOrder(): Array<Any> {
        return arrayOf(guid, time)
    }

    companion object {
        fun from(data: ByteBuf): ConnectionRequestPacket {
            return ConnectionRequestPacket(
                data.readLong(),
                data.readLong()
            )
        }
    }

    override fun toString(): String {
        return "ConnectionRequestPacket(guid=$guid, time=$time)"
    }
}