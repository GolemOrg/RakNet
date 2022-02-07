package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.OfflinePacket
import raknet.packet.MessageType
import raknet.readAddress
import java.net.InetSocketAddress

class OpenConnectionRequest2(
    var magic: Magic,
    var serverAddress: InetSocketAddress,
    var mtuSize: Short,
    var clientGuid: Long,
): OfflinePacket(MessageType.OPEN_CONNECTION_REQUEST_2.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(magic, serverAddress, mtuSize, clientGuid)

    companion object {
        fun from(buffer: ByteBuf) = OpenConnectionRequest2(
            buffer.readMagic(),
            buffer.readAddress(),
            buffer.readUnsignedShort().toShort(),
            buffer.readLong()
        )
    }

    override fun toString() = "OpenConnectionRequest2(magic=$magic, serverAddress=$serverAddress, mtuSize=$mtuSize, clientGuid=$clientGuid)"

}