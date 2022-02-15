package raknet

import raknet.connection.Connection

sealed class ServerEvent {
    object Start: ServerEvent()
    class NewConnection(val connection: Connection) : ServerEvent()
    object Shutdown : ServerEvent()
}
