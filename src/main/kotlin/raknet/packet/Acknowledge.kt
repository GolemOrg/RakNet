package raknet.packet

import io.netty.buffer.ByteBuf
import raknet.UIntLE
import raknet.codec.Codable

data class Record(
    val isSingle: Boolean,
    val sequenceNumber: UIntLE,
    val endSequenceNumber: UIntLE? = null
): Codable {
    init {
        if(!isSingle && endSequenceNumber == null) throw IllegalArgumentException("Ranged record must have end sequence number")
    }

    override fun encode(buffer: ByteBuf) {
        buffer.writeBoolean(isSingle)
        buffer.writeInt(sequenceNumber.toInt())
        if(!isSingle) buffer.writeInt(endSequenceNumber!!.toInt())
    }

    override fun decode(buffer: ByteBuf) = Record(
        isSingle = buffer.readBoolean(),
        sequenceNumber = buffer.readInt().toUInt(),
        endSequenceNumber = if(!isSingle) buffer.readInt().toUInt() else null
    )

    companion object {
        fun from(sequenceNumber: UIntLE, endSequenceNumber: UIntLE? = null) = Record(
            isSingle = endSequenceNumber == null,
            sequenceNumber = sequenceNumber,
            endSequenceNumber = endSequenceNumber
        )
    }

    override fun toString(): String = "Record(isSingle=$isSingle, sequenceNumber=$sequenceNumber, endSequenceNumber=$endSequenceNumber)"
}

sealed class Base(id: Short, val recordCount: Short, val record: Record): ConnectedPacket(id) {
    override fun encodeOrder(): Array<Any> = arrayOf(recordCount, record)
}

class Acknowledge(recordCount: Short, record: Record): Base(PacketType.ACK.id(), recordCount, record) {
    override fun toString(): String = "Acknowledge(recordCount=$recordCount, record=$record)"
}
class NAcknowledge(recordCount: Short, record: Record): Base(PacketType.NAK.id(), recordCount, record) {
    override fun toString(): String = "NAcknowledge(recordCount=$recordCount, record=$record)"
}