package raknet.enums

import kotlin.experimental.and

enum class ReliabilityType(
    val reliable: Boolean,
    val ordered: Boolean,
    val sequenced: Boolean,
) {
    UNRELIABLE(false, false, false),
    UNRELIABLE_SEQUENCED(false, true, true),
    RELIABLE(true, false, false),
    RELIABLE_ORDERED(true, true, false),
    RELIABLE_SEQUENCED(true, true, true),

    UNRELIABLE_WITH_ACK_RECEIPT(false, false, false),
    RELIABLE_WITH_ACK_RECEIPT(true, false, false),
    RELIABLE_ORDERED_WITH_ACK_RECEIPT(true, true, false);

    companion object {

        /**
         * The bits that represent the reliability type.
         */
        private const val RELIABLE_FLAG = 0b100.toByte()
        private const val ORDERED_FLAG = 0b010.toByte()
        private const val SEQUENCED_FLAG = 0b001.toByte()

        /**
         * Values to use when converting the raw flags to a set of flags
         */
        private const val MASK = 0b1110_0000.toByte()
        private const val SHIFT_VALUE = 4

        fun from(flags: Byte, withAckReceipt: Boolean = false): ReliabilityType {
            val reliable: Boolean = flags and RELIABLE_FLAG != 0.toByte()
            val ordered: Boolean = flags and ORDERED_FLAG != 0.toByte()
            val sequenced: Boolean = flags and SEQUENCED_FLAG != 0.toByte()
            return when {
                reliable && ordered && sequenced -> RELIABLE_SEQUENCED
                reliable && ordered -> if(withAckReceipt) RELIABLE_ORDERED_WITH_ACK_RECEIPT else RELIABLE_ORDERED
                reliable && sequenced -> RELIABLE_SEQUENCED
                reliable -> if(withAckReceipt) RELIABLE_WITH_ACK_RECEIPT else RELIABLE
                sequenced -> UNRELIABLE_SEQUENCED
                else -> if (withAckReceipt) UNRELIABLE_WITH_ACK_RECEIPT else UNRELIABLE
            }
        }

        fun fromRaw(flags: Byte, withAckReceipt: Boolean = false): ReliabilityType {
            return from(applyMask(flags), withAckReceipt)
        }

        /**
         * This is used to get the reliability flags and then shift them to match the flags above.
         */
        fun applyMask(flags: Byte): Byte = flags.and(MASK).toInt().shr(SHIFT_VALUE).toByte()
    }
}
