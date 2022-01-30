package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.ConnectedPacket
import raknet.packet.PacketType
import raknet.readAddress
import java.net.InetSocketAddress

class NewIncomingConnection(
    var address: InetSocketAddress,
    var internalAddress: InetSocketAddress,
): ConnectedPacket(PacketType.NEW_INCOMING_CONNECTION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(address, internalAddress)

    companion object {
        fun from(buffer: ByteBuf): NewIncomingConnection {
            return NewIncomingConnection(
                buffer.readAddress(),
                buffer.readAddress()
            )
        }
    }

    override fun toString(): String = "NewIncomingConnectionPacket(address=$address, internalAddress=$internalAddress)"
}