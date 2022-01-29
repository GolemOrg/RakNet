package raknet.enums

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
    RELIABLE_ORDERED_WITH_ACK_RECEIPT(true, true, false),
}
