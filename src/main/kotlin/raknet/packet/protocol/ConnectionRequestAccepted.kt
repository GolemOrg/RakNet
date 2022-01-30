package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.enums.AddressCount
import raknet.packet.DataPacket
import raknet.packet.PacketType
import raknet.readAddress
import java.net.InetSocketAddress

class ConnectionRequestAccepted(
    var address: InetSocketAddress,
    var systemIndex: Int,
    var internalIds: Array<InetSocketAddress>,
    var requestTime: Long,
    var time: Long
): DataPacket(PacketType.CONNECTION_REQUEST_ACCEPTED.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(address, systemIndex, internalIds, requestTime, time)
    }

    companion object {
        fun from(buffer: ByteBuf): ConnectionRequestAccepted {
            return ConnectionRequestAccepted(
                buffer.readAddress(),
                buffer.readInt(),
                (0 until AddressCount.MINECRAFT.count()).map { buffer.readAddress() }.toTypedArray(),
                buffer.readLong(),
                buffer.readLong()
            )
        }
    }

    override fun toString(): String {
        return "ConnectionRequestAcceptedPacket(address=$address, systemIndex=$systemIndex, internalIds=${internalIds.contentToString()}, requestTime=$requestTime, time=$time)"
    }
}