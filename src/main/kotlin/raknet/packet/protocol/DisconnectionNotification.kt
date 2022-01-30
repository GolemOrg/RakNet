package raknet.packet.protocol

import raknet.packet.DataPacket
import raknet.packet.PacketType

class DisconnectionNotification(): DataPacket(PacketType.DISCONNECTION_NOTIFICATION.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf()
    }

    override fun toString(): String {
        return "DisconnectionNotification()"
    }
}