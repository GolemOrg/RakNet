package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.DataPacket
import raknet.packet.PacketType

class OpenConnectionRequest1(
    var magic: Magic,
    var protocolVersion: Int,
    var mtuSize: Short
): DataPacket(PacketType.OPEN_CONNECTION_REQUEST_1.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(magic, protocolVersion, mtuSize)
    }

    companion object {
        fun from(buffer: ByteBuf): OpenConnectionRequest1 {
            return OpenConnectionRequest1(
                buffer.readMagic(),
                buffer.readInt(),
                buffer.readableBytes().toShort()
            )
        }
    }

    override fun toString(): String {
        return "OpenConnectionRequest1Packet(magic=$magic, protcolVersion=$protocolVersion, mtuSize=$mtuSize)"
    }
}