package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.types.Magic
import raknet.types.Magic.readMagic
import raknet.packet.OfflinePacket
import raknet.packet.MessageType

class UnconnectedPing(
    var time: Long,
    var magic: Magic,
    var clientGuid: Long
): OfflinePacket(MessageType.UNCONNECTED_PING.id()) {

    override fun encodeOrder(): Array<Any> =  arrayOf(time, magic, clientGuid)

    companion object {
        fun from(buffer: ByteBuf) = UnconnectedPing(
            buffer.readLong(),
            buffer.readMagic(),
            buffer.readLong()
        )
    }

    override fun toString() = "UnconnectedPing(time=$time, clientGuid=$clientGuid)"
}