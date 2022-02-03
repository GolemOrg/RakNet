package raknet

import raknet.connection.Connection
import raknet.packet.protocol.ConnectionRequest
import raknet.packet.protocol.OpenConnectionRequest1
import raknet.packet.protocol.OpenConnectionRequest2
import raknet.packet.protocol.UnconnectedPing
import java.net.InetSocketAddress

open class ServerListener {

    open fun handleUnconnectedPing(address: InetSocketAddress, ping: UnconnectedPing) {}

    open fun handleOpenConnectionRequest1(address: InetSocketAddress, request: OpenConnectionRequest1) {}

    open fun handleOpenConnectionRequest2(address: InetSocketAddress, request: OpenConnectionRequest2) {}

    open fun handleNewConnection(connection: Connection) {}

}