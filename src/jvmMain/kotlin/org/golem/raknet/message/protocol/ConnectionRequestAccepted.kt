package org.golem.raknet.message.protocol

import io.netty.buffer.ByteBuf
import org.golem.raknet.enums.AddressCount
import org.golem.raknet.message.OnlineMessage
import org.golem.raknet.message.MessageType
import org.golem.raknet.readAddress
import java.net.InetSocketAddress

class ConnectionRequestAccepted(
    var clientAddress: InetSocketAddress,
    var systemIndex: Short,
    var internalIds: Array<InetSocketAddress> = DEFAULT_ADDRESSES,
    var requestTime: Long,
    var time: Long
): OnlineMessage(MessageType.CONNECTION_REQUEST_ACCEPTED.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(clientAddress, systemIndex, internalIds, requestTime, time)

    companion object {
        val DEFAULT_ADDRESSES: Array<InetSocketAddress> = Array(AddressCount.RAKNET.toInt()) { InetSocketAddress("0.0.0.0", 0) }

        fun from(buffer: ByteBuf) = ConnectionRequestAccepted(
            buffer.readAddress(),
            buffer.readShort(),
            (0 until AddressCount.RAKNET.toInt()).map { buffer.readAddress() }.toTypedArray(),
            buffer.readLong(),
            buffer.readLong()
        )
    }

    override fun toString() = "ConnectionRequestAccepted(clientAddress=$clientAddress, systemIndex=$systemIndex, internalIds=${internalIds.contentToString()}, requestTime=$requestTime, time=$time)"
}