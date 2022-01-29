package raknet.packet.protocol.connected.reply

import io.netty.buffer.ByteBuf
import raknet.Magic
import raknet.Magic.readMagic
import raknet.packet.DataPacket
import raknet.packet.PacketType

class OpenConnectionReply1Packet(
    var magic: Magic,
    var serverGuid: Long,
    var useSecurity: Boolean,
    var mtuSize: Int
): DataPacket(PacketType.OPEN_CONNECTION_REPLY_1.id()) {


    override fun encodeOrder(): Array<Any> {
        return arrayOf(magic, serverGuid, useSecurity, mtuSize)
    }

    companion object {

        fun from(buffer: ByteBuf): OpenConnectionReply1Packet {
            return OpenConnectionReply1Packet(
                buffer.readMagic(),
                buffer.readLong(),
                buffer.readBoolean(),
                buffer.readInt()
            )
        }
    }

    override fun toString(): String {
        return "OpenConnectionReply1Packet(magic=$magic, serverGuid=$serverGuid, useSecurity=$useSecurity, mtuSize=$mtuSize)"
    }
}