package raknet.message

enum class MessageType(private val id: Int) {
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
    NACK(0xa0),

    USER_PACKET_ENUM(0x86);

    fun id(): Int = id

    companion object {
        fun find(id: Int): MessageType? = values().firstOrNull { it.id == id }
    }
}