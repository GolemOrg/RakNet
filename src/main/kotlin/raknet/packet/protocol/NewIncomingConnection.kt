package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.ConnectedPacket
import raknet.packet.MessageType
import raknet.readAddress
import java.net.InetSocketAddress

class NewIncomingConnection(
    var address: InetSocketAddress,
    var internalAddress: InetSocketAddress,
): ConnectedPacket(MessageType.NEW_INCOMING_CONNECTION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(address, internalAddress)

    companion object { fun from(buffer: ByteBuf) = NewIncomingConnection(buffer.readAddress(), buffer.readAddress()) }

    override fun toString() = "NewIncomingConnection(address=$address, internalAddress=$internalAddress)"
}