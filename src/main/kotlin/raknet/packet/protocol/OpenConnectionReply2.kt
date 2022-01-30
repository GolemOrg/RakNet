package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.DataPacket
import raknet.packet.PacketType
import raknet.readAddress
import java.net.InetSocketAddress

class OpenConnectionReply2(
    var magic: Magic,
    var serverGuid: Long,
    var clientAddress: InetSocketAddress,
    var mtuSize: Short,
    var encryptionEnabled: Boolean,
): DataPacket(PacketType.OPEN_CONNECTION_REPLY_2.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(magic, serverGuid, clientAddress, mtuSize, encryptionEnabled)
    }

    companion object {
        fun from(buffer: ByteBuf): OpenConnectionReply2 {
            return OpenConnectionReply2(
                buffer.readMagic(),
                buffer.readLong(),
                buffer.readAddress(),
                buffer.readShort(),
                buffer.readBoolean()
            )
        }
    }

    override fun toString(): String {
        return "OpenConnectionReply2Packet(magic=$magic, serverGuid=$serverGuid, clientAddress=$clientAddress, mtuSize=$mtuSize, encryptionEnabled=$encryptionEnabled)"
    }
}