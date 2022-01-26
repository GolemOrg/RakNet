package raknet.enums

enum class BitFlag(private val id: Short) {
    DATAGRAM(0x80),
    ACK(0x40),
    NACK(0x20);

    fun id(): Short = id
}