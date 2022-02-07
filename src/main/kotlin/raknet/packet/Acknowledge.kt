package raknet.packet

import io.netty.buffer.ByteBuf
import raknet.codec.Decodable
import raknet.codec.Encodable
import raknet.types.UIntLE

data class Record(val sequenceNumber: UIntLE, val endSequenceNumber: UIntLE? = null): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) {}

    override fun decode(buffer: ByteBuf): Any = Unit

    override fun toString() = "Record(sequenceNumber=$sequenceNumber, endSequenceNumber=$endSequenceNumber)"
}

sealed class Base(id: Int, val records: MutableList<Record>): ConnectedPacket(id) {
    override fun encodeOrder(): Array<Any> = arrayOf(records.size.toShort(), records)
}

class Acknowledge(records: MutableList<Record>): Base(MessageType.ACK.id(), records) {
    override fun toString(): String = "Acknowledge(records=${records.joinToString()})"
}
class NAcknowledge(records: MutableList<Record>): Base(MessageType.NACK.id(), records) {
    override fun toString(): String = "NAcknowledge(records=${records.joinToString()})"
}