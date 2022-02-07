package raknet.connection

sealed class DisconnectionReason {
    object ServerClosed: DisconnectionReason() {
        override fun toString(): String = "Server closed"
    }
    object ClientRequested: DisconnectionReason() {
        override fun toString(): String = "Client disconnect"
    }
    object IncompatibleProtocol: DisconnectionReason() {
        override fun toString(): String = "Incompatible Protocol"
    }
    object Timeout: DisconnectionReason() {
        override fun toString(): String = "Timeout"
    }
    object Error: DisconnectionReason() {
        override fun toString(): String = "Internal Error"
    }
    class Custom(val reason: String) : DisconnectionReason() {
        override fun toString(): String = reason
    }
    object Unknown: DisconnectionReason() {
        override fun toString(): String = "Unknown"
    }
}
