package raknet.packet.protocol

import raknet.packet.ConnectedPacket
import raknet.packet.MessageType

class DisconnectionNotification : ConnectedPacket(MessageType.DISCONNECTION_NOTIFICATION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf()

    override fun toString() = "DisconnectionNotification()"
}