package raknet.connection

import raknet.packet.DataPacket
import raknet.packet.protocol.UnknownPacket

open class ConnectionListener {

    open fun handlePacket(packet: DataPacket) {}

    open fun handleConnected(connection: Connection) {}

    open fun handleDisconnected(connection: Connection, reason: String) {}
}