package raknet.enums

enum class Reliability {
    UNRELIABLE,
    UNRELIABLE_SEQUENCED,
    RELIABLE,
    RELIABLE_ORDERED,
    RELIABLE_SEQUENCED,
    UNRELIABLE_WITH_ACK_RECEIPT,
    RELIABLE_WITH_ACK_RECEIPT,
    RELIABLE_ORDERED_WITH_ACK_RECEIPT;

    fun reliable(): Boolean = this == RELIABLE || this == RELIABLE_ORDERED || this == RELIABLE_SEQUENCED || this == RELIABLE_WITH_ACK_RECEIPT || this == RELIABLE_ORDERED_WITH_ACK_RECEIPT
    fun sequenced(): Boolean = this == UNRELIABLE_SEQUENCED || this == RELIABLE_SEQUENCED
    fun ordered(): Boolean = sequenced() || this == RELIABLE_ORDERED || this == RELIABLE_ORDERED_WITH_ACK_RECEIPT
    fun ack(): Boolean = this == UNRELIABLE_WITH_ACK_RECEIPT || this == RELIABLE_WITH_ACK_RECEIPT || this == RELIABLE_ORDERED_WITH_ACK_RECEIPT

    fun toRaw() = ordinal shl RAW_SHIFT_SIZE
    fun toByte() = ordinal.toByte()

    companion object {

        /**
         * This is how many bits we have to shift to convert between the raw byte & ordinal values.
         */
        private const val RAW_SHIFT_SIZE = 5

        fun from(value: Int): Reliability = values().first { it.ordinal == value }
        fun fromRaw(value: Int): Reliability = from(value ushr RAW_SHIFT_SIZE)

    }
}
