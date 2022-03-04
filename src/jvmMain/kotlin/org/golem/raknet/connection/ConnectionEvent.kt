package org.golem.raknet.connection

import org.golem.raknet.message.UserMessage

sealed class ConnectionEvent {
    object Connected : ConnectionEvent() {
        override fun toString() = "ConnectedEvent"
    }
    class ReceivedMessage(val message: UserMessage) : ConnectionEvent() {
        override fun toString() = "ReceivedMessageEvent(message=$message)"
    }
    class LatencyUpdated(val latency: Long) : ConnectionEvent() {
        override fun toString() = "LatencyUpdatedEvent(latency=$latency)"
    }
    class Disconnected(val reason: DisconnectionReason): ConnectionEvent() {
        override fun toString() = "DisconnectedEvent(reason=$reason)"
    }
}