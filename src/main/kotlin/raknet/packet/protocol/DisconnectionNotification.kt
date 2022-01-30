package raknet.packet.protocol

import raknet.packet.ConnectedPacket
import raknet.packet.PacketType

class DisconnectionNotification : ConnectedPacket(PacketType.DISCONNECTION_NOTIFICATION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf()

    override fun toString(): String = "DisconnectionNotification()"
}