package org.golem.raknet.connection

import org.golem.raknet.message.OnlineMessage

sealed class ConnectionEvent {
    object Connected: ConnectionEvent()
    class Received(val message: OnlineMessage) : ConnectionEvent()
    class LatencyUpdated(val latency: Long) : ConnectionEvent()
    class Disconnected(val reason: DisconnectionReason): ConnectionEvent()
}
