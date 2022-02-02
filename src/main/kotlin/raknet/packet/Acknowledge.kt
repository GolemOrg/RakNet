package raknet.packet

import io.netty.buffer.ByteBuf
import raknet.UIntLE
import raknet.codec.Codable

data class Record(val isSingle: Boolean, val sequenceNumber: UIntLE, val endSequenceNumber: UIntLE? = null): Codable {
    init { if(!isSingle && endSequenceNumber == null) throw IllegalArgumentException("End sequence number must be set for ranged record") }

    override fun encode(buffer: ByteBuf) {
        buffer.writeBoolean(isSingle)
        buffer.writeMediumLE(sequenceNumber.toInt())
        if(!isSingle) buffer.writeMediumLE(endSequenceNumber!!.toInt())
    }

    override fun decode(buffer: ByteBuf) = Record(
        isSingle = buffer.readBoolean(),
        sequenceNumber = buffer.readUnsignedMediumLE().toUInt(),
        endSequenceNumber = if(!isSingle) buffer.readUnsignedMediumLE().toUInt() else null
    )

    companion object {
        fun from(buffer: ByteBuf): Record {
            val single = buffer.readBoolean()
            return Record(
                isSingle = single,
                sequenceNumber = buffer.readUnsignedMediumLE().toUInt(),
                endSequenceNumber = if(!single) buffer.readUnsignedMediumLE().toUInt() else null
            )
        }
    }

    override fun toString() = "Record(isSingle=$isSingle, sequenceNumber=$sequenceNumber, endSequenceNumber=$endSequenceNumber)"
}

sealed class Base(id: Int, val recordCount: Short, val record: Record): ConnectedPacket(id) {
    override fun encodeOrder(): Array<Any> = arrayOf(recordCount, record)
}

class Acknowledge(recordCount: Short, record: Record): Base(PacketType.ACK.id(), recordCount, record) {
    override fun toString() = "Acknowledge(recordCount=$recordCount, record=$record)"

    companion object { fun from(buffer: ByteBuf) = Acknowledge(buffer.readShort(), Record.from(buffer)) }
}
class NAcknowledge(recordCount: Short, record: Record): Base(PacketType.NACK.id(), recordCount, record) {
    override fun toString() = "NAcknowledge(recordCount=$recordCount, record=$record)"

    companion object { fun from(buffer: ByteBuf) = NAcknowledge(buffer.readShort(), Record.from(buffer)) }
}