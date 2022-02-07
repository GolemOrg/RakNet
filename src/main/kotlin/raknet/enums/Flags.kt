package raknet.enums

enum class Flags(private val id: Int) {
    DATAGRAM(0x80),
    ACK(0x40),
    NACK(0x20),
    PACKET_PAIR(0x10),
    CONTINUOUS_SEND(0x08),
    NEEDS_B_AND_AS(0x04);


    fun id(): Int = id

    companion object {
        fun from(id: Int): Array<Flags> = values().filter { it.id and id != 0}.toTypedArray()
    }
}