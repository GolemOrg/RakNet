package raknet.packet.protocol.connected.request

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.DataPacket
import raknet.packet.PacketType

class OpenConnectionRequest1Packet(
    var magic: Magic,
    var protocolVersion: Int,
    var mtuSize: Int
): DataPacket(PacketType.OPEN_CONNECTION_REQUEST_1.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(magic, protocolVersion, mtuSize)
    }

    companion object {
        fun from(buffer: ByteBuf): OpenConnectionRequest1Packet {
            return OpenConnectionRequest1Packet(
                buffer.readMagic(),
                buffer.readInt(),
                buffer.readInt()
            )
        }
    }

    override fun toString(): String {
        return "OpenConnectionRequest1Packet(magic=$magic, protcolVersion=$protocolVersion, mtuSize=$mtuSize)"
    }
}