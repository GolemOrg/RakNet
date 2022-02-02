package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.enums.AddressCount
import raknet.packet.ConnectedPacket
import raknet.packet.PacketType
import raknet.readAddress
import java.net.InetSocketAddress

class ConnectionRequestAccepted(
    var clientAddress: InetSocketAddress,
    var systemIndex: Int,
    var internalIds: Array<InetSocketAddress> = DEFAULT_ADDRESSES,
    var requestTime: Long,
    var time: Long
): ConnectedPacket(PacketType.CONNECTION_REQUEST_ACCEPTED.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(clientAddress, systemIndex, internalIds, requestTime, time)

    companion object {
        val DEFAULT_ADDRESSES: Array<InetSocketAddress> = Array(AddressCount.MINECRAFT.count()) { InetSocketAddress("0.0.0.0", 0) }

        fun from(buffer: ByteBuf) = ConnectionRequestAccepted(
            buffer.readAddress(),
            buffer.readInt(),
            (0 until AddressCount.RAKNET.count()).map { buffer.readAddress() }.toTypedArray(),
            buffer.readLong(),
            buffer.readLong()
        )
    }

    override fun toString() = "ConnectionRequestAccepted(clientAddress=$clientAddress, systemIndex=$systemIndex, internalIds=${internalIds.contentToString()}, requestTime=$requestTime, time=$time)"
}