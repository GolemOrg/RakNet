package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.types.Magic
import raknet.types.Magic.readMagic
import raknet.packet.OfflineMessage
import raknet.packet.MessageType
import raknet.readAddress
import java.net.InetSocketAddress

class OpenConnectionRequest2(
    var magic: Magic,
    var serverAddress: InetSocketAddress,
    var mtuSize: Short,
    var clientGuid: Long,
): OfflineMessage(MessageType.OPEN_CONNECTION_REQUEST_2.id()) {

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