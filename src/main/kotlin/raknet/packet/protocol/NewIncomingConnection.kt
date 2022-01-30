package raknet.packet.protocol

import io.netty.buffer.ByteBuf
import raknet.packet.DataPacket
import raknet.packet.PacketType
import raknet.readAddress
import java.net.InetSocketAddress

class NewIncomingConnection(
    var address: InetSocketAddress,
    var internalAddress: InetSocketAddress,
): DataPacket(PacketType.NEW_INCOMING_CONNECTION.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(address, internalAddress)
    }

    companion object {
        fun from(buffer: ByteBuf): NewIncomingConnection {
            return NewIncomingConnection(
                buffer.readAddress(),
                buffer.readAddress()
            )
        }
    }

    override fun toString(): String {
        return "NewIncomingConnectionPacket(address=$address, internalAddress=$internalAddress)"
    }
}