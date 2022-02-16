package org.golem.raknet.message.protocol

import org.golem.raknet.message.OnlineMessage
import org.golem.raknet.message.MessageType

class DisconnectionNotification : OnlineMessage(MessageType.DISCONNECTION_NOTIFICATION.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf()

    override fun toString() = "DisconnectionNotification()"
}