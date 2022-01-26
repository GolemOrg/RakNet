package raknet.packet

enum class PacketType(private val id: Short) {
    CONNECTED_PING(0x00),
    CONNECTED_PONG(0x03),

    UNCONNECTED_PING(0x01),
    UNCONNECTED_PONG(0x1c),

    OPEN_CONNECTION_REQUEST_1(0x05),
    OPEN_CONNECTION_REPLY_1(0x06),

    OPEN_CONNECTION_REQUEST_2(0x07),
    OPEN_CONNECTION_REPLY_2(0x08),

    CONNECTION_REQUEST(0x09),
    CONNECTION_REQUEST_ACCEPTED(0x10),

    NEW_INCOMING_CONNECTION(0x13),
    DISCONNECTION_NOTIFICATION(0x15),

    INCOMPATIBLE_PROTOCOL_VERSION(0x19),

    ACK(0xc0),
    NAK(0xa0);

    fun id(): Short {
        return id
    }

    companion object {
        fun find(id: Short): PacketType? {
            return values().firstOrNull { it.id == id }
        }
    }
}