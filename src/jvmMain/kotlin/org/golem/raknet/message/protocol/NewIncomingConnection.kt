package org.golem.raknet.message.protocol

import io.netty.buffer.ByteBuf
import org.golem.raknet.message.OnlineMessage
import org.golem.raknet.message.MessageType
import org.golem.raknet.readAddress
import java.net.InetSocketAddress

class NewIncomingConnection(
    var address: InetSocketAddress,
    var internalAddress: InetSocketAddress,
): OnlineMessage(MessageType.NEW_INCOMING_CONNECTION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(address, internalAddress)

    companion object { fun from(buffer: ByteBuf) = NewIncomingConnection(buffer.readAddress(), buffer.readAddress()) }

    override fun toString() = "NewIncomingConnection(address=$address, internalAddress=$internalAddress)"
}