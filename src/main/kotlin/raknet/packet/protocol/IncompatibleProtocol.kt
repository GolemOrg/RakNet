package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.ConnectedPacket
import raknet.packet.PacketType

class IncompatibleProtocol(
    var protocol: Int,
    var serverGuid: Long,
): ConnectedPacket(PacketType.INCOMPATIBLE_PROTOCOL_VERSION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(protocol, serverGuid)

    companion object {
        fun from(buffer: ByteBuf) = IncompatibleProtocol(buffer.readInt(), buffer.readLong())
    }

    override fun toString() = "IncompatibleProtocolPacket(protocol=$protocol, serverGuid=$serverGuid)"
}