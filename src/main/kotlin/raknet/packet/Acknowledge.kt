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

    override fun decode(buffer: ByteBuf): Any {
        return Record(
            isSingle = buffer.readBoolean(),
            sequenceNumber = buffer.readInt().toUInt(),
            endSequenceNumber = if(!isSingle) buffer.readInt().toUInt() else null
        )
    }

    companion object {
        fun from(sequenceNumber: UIntLE, endSequenceNumber: UIntLE? = null): Record {
            return Record(
                isSingle = endSequenceNumber == null,
                sequenceNumber = sequenceNumber,
                endSequenceNumber = endSequenceNumber
            )
        }
    }

    override fun toString(): String = "Record(isSingle=$isSingle, sequenceNumber=$sequenceNumber, endSequenceNumber=$endSequenceNumber)"
}

class Acknowledge(val recordCount: Short, val record: Record): ConnectedPacket(PacketType.ACK.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(recordCount, record)

}

class NAcknowledge(val recordCount: Short, val record: Record): ConnectedPacket(PacketType.NAK.id()) {
    override fun encodeOrder(): Array<Any> = arrayOf(recordCount, record)
}