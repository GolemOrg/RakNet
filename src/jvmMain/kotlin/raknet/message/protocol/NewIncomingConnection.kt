package raknet.message.protocol

import io.netty.buffer.ByteBuf
import raknet.message.OnlineMessage
import raknet.message.MessageType
import raknet.readAddress
import java.net.InetSocketAddress

class NewIncomingConnection(
    var address: InetSocketAddress,
    var internalAddress: InetSocketAddress,
): OnlineMessage(MessageType.NEW_INCOMING_CONNECTION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(address, internalAddress)

    companion object { fun from(buffer: ByteBuf) = NewIncomingConnection(buffer.readAddress(), buffer.readAddress()) }

    override fun toString() = "NewIncomingConnection(address=$address, internalAddress=$internalAddress)"
}