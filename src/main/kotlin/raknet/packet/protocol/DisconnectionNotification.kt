package raknet.packet.protocol

import raknet.packet.OnlineMessage
import raknet.packet.MessageType

class DisconnectionNotification : OnlineMessage(MessageType.DISCONNECTION_NOTIFICATION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf()

    override fun toString() = "DisconnectionNotification()"
}