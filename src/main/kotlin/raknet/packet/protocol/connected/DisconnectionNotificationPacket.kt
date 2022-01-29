package raknet.packet.protocol.connected

import raknet.packet.DataPacket
import raknet.packet.PacketType

class DisconnectionNotificationPacket(): DataPacket(PacketType.DISCONNECTION_NOTIFICATION.id()) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf()
    }
}