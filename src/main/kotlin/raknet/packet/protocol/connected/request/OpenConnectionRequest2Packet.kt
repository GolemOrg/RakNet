package raknet.packet.protocol.connected.request

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.DataPacket
import raknet.packet.PacketType
import raknet.readAddress
import java.net.InetSocketAddress

class OpenConnectionRequest2Packet(
    var magic: Magic,
    var serverAddress: InetSocketAddress,
    var mtuSize: Int,
    var clientGuid: Long,
): DataPacket(PacketType.OPEN_CONNECTION_REQUEST_2.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(magic, serverAddress, mtuSize, clientGuid)
    }

    companion object {
        fun from(data: ByteBuf): OpenConnectionRequest2Packet {
            return OpenConnectionRequest2Packet(
                data.readMagic(),
                data.readAddress(),
                data.readUnsignedShort(),
                data.readLong()
            )
        }
    }

    override fun toString(): String {
        return "OpenConnectionRequest2Packet(magic=$magic, serverAddress=$serverAddress, mtuSize=$mtuSize, clientGuid=$clientGuid)"
    }

}