package raknet.connection

import raknet.packet.Packet
import java.net.InetSocketAddress

class Connection(private val address: InetSocketAddress) {
    enum class State { INITIALIZING, CONNECTING, CONNECTED, DISCONNECTED }

    private var state: State = State.INITIALIZING
    // TODO: Listeners


    private fun tick() {

    }

    private fun handle(packet: Packet) {

    }

    private fun close() {

    }

}

