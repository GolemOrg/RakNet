package raknet.connection

import raknet.message.Acknowledge
import raknet.message.NAcknowledge
import raknet.message.datagram.Datagram

class InternalsHandler(val connection: Connection) {

    val ackQueue = mutableListOf<Acknowledge>()
    val nakQueue = mutableListOf<NAcknowledge>()

    fun handle(datagram: Datagram) {

    }

    fun handle(acknowledge: Acknowledge) {

    }

    fun handle(nAcknowledge: NAcknowledge) {

    }
}