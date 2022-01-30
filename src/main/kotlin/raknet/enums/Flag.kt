package raknet.enums

import kotlin.experimental.and

enum class Flag(private val id: Short) {
    DATAGRAM(0b1000_0000),
    ACK(0b0100_0000),
    NACK(0b0010_0000),
    HAS_B_AND_AS(0b0010_0000),
    PACKET_PAIR(0b0001_0000),
    CONTINUOUS_SEND(0b0000_1000),
    NEEDS_B_AND_AS(0b0000_0100);


    fun id(): Short = id

    companion object {
        fun from(id: Short): Array<Flag> {
            return values().filter { it.id and id != 0.toShort()}.toTypedArray()
        }
    }
}