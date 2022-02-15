package raknet.message

import io.netty.buffer.ByteBuf
import raknet.codec.Decodable
import raknet.codec.OrderedEncodable
import raknet.encode
import raknet.types.UInt24LE

sealed class Record(val sequenceNumber: UInt24LE): OrderedEncodable, Decodable {

    class Single(sequenceNumber: UInt24LE): Record(sequenceNumber) {
        override fun getCount(): Int = 1
        override fun asList(): List<UInt24LE> = listOf(sequenceNumber)
        override fun encodeOrder(): Array<Any> = arrayOf(sequenceNumber)
        override fun decode(buffer: ByteBuf): Any = Unit
        override fun toString(): String = "Record.Single(sequenceNumber=$sequenceNumber)"
    }

    class Range(sequenceNumber: UInt24LE, val endSequenceNumber: UInt24LE): Record(sequenceNumber) {
        override fun getCount(): Int = endSequenceNumber.toInt() - sequenceNumber.toInt() + 1
        override fun asList(): List<UInt24LE> = (sequenceNumber.toInt()..endSequenceNumber.toInt()).map { UInt24LE(it.toUInt()) }
        override fun encodeOrder(): Array<Any> = arrayOf(sequenceNumber, endSequenceNumber)
        override fun decode(buffer: ByteBuf): Any = Unit
        override fun toString(): String = "Record.Range(sequenceNumber=$sequenceNumber, endSequenceNumber=$endSequenceNumber)"
    }

    abstract fun getCount(): Int
    abstract fun asList(): List<UInt24LE>

    override fun encode(buffer: ByteBuf) {
        buffer.writeBoolean(getCount() == 1) // Is Single?
        encodeOrder().encode(buffer)
    }
}

sealed class Base(id: Int, val records: MutableList<Record>): OnlineMessage(id) {
    override fun encodeOrder(): Array<Any> = arrayOf(records.size.toShort(), records)
}

class Acknowledge(records: MutableList<Record>): Base(MessageType.ACK.id(), records) {
    override fun toString(): String = "Acknowledge(records=$records)"

    companion object  {
        fun fromQueue(queue: MutableList<UInt>): Acknowledge = Acknowledge(compileRecords(queue))
    }
}
class NAcknowledge(records: MutableList<Record>): Base(MessageType.NACK.id(), records) {
    override fun toString(): String = "NAcknowledge(records=$records)"

    companion object  {
        fun fromQueue(queue: MutableList<UInt>): NAcknowledge = NAcknowledge(compileRecords(queue))
    }
}

private fun compileRecords(queue: MutableList<UInt>): MutableList<Record> {
    val records = mutableListOf<Record>()
    val iterator = queue.iterator()
    var start = iterator.next()
    var end = start
    while(iterator.hasNext()) {
        val next = iterator.next()
        if(end - next > 1u) {
            records.add(if(start != end) Record.Range(UInt24LE(start), UInt24LE(end)) else Record.Single(UInt24LE(start)))
            start = next
            end = next
        } else {
            end = next
        }
    }
    records.add(if(start != end) Record.Range(UInt24LE(start), UInt24LE(end)) else Record.Single(UInt24LE(start)))
    return records
}