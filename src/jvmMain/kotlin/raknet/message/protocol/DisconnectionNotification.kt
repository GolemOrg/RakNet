package raknet.message.protocol

import raknet.message.OnlineMessage
import raknet.message.MessageType

class DisconnectionNotification : OnlineMessage(MessageType.DISCONNECTION_NOTIFICATION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf()

    override fun toString() = "DisconnectionNotification()"
}