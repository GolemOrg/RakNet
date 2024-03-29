package org.golem.raknet.message

import io.netty.buffer.ByteBuf
import org.golem.netty.codec.OrderedEncodable
import org.golem.netty.codec.encode
import org.golem.netty.types.UMediumLE

sealed class Record(val sequenceNumber: UMediumLE): OrderedEncodable {

    class Single(sequenceNumber: UMediumLE): Record(sequenceNumber) {
        override fun getCount(): Int = 1
        override fun asList(): List<UMediumLE> = listOf(sequenceNumber)
        override fun encodeOrder(): Array<Any> = arrayOf(sequenceNumber)
        override fun toString(): String = "Record.Single(sequenceNumber=$sequenceNumber)"
    }

    class Range(beginningSequenceNumber: UMediumLE, val endSequenceNumber: UMediumLE): Record(beginningSequenceNumber) {
        override fun getCount(): Int = endSequenceNumber.toInt() - sequenceNumber.toInt() + 1
        override fun asList(): List<UMediumLE> = (sequenceNumber.toInt()..endSequenceNumber.toInt()).map { UMediumLE(it.toUInt()) }
        override fun encodeOrder(): Array<Any> = arrayOf(sequenceNumber, endSequenceNumber)
        override fun toString(): String = "Record.Range(sequenceNumber=$sequenceNumber, endSequenceNumber=$endSequenceNumber)"
    }

    abstract fun getCount(): Int
    abstract fun asList(): List<UMediumLE>

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
        fun from(buffer: ByteBuf): Acknowledge = Acknowledge(decompressRecords(buffer))
        fun fromQueue(queue: MutableList<UInt>): Acknowledge = Acknowledge(compressRecords(queue))
    }
}
class NAcknowledge(records: MutableList<Record>): Base(MessageType.NACK.id(), records) {
    override fun toString(): String = "NAcknowledge(records=$records)"

    companion object  {
        fun from(buffer: ByteBuf): NAcknowledge = NAcknowledge(decompressRecords(buffer))
        fun fromQueue(queue: MutableList<UInt>): NAcknowledge = NAcknowledge(compressRecords(queue))
    }
}

private fun decompressRecords(buffer: ByteBuf): MutableList<Record> {
    val records = mutableListOf<Record>()
    val count = buffer.readShort()
    for (i in 0 until count) {
        val isSingle = buffer.readBoolean()
        val record = if (isSingle) {
            Record.Single(sequenceNumber = UMediumLE(buffer.readUnsignedMediumLE().toUInt()))
        } else {
            Record.Range(
                beginningSequenceNumber = UMediumLE(buffer.readUnsignedMediumLE().toUInt()),
                endSequenceNumber = UMediumLE(buffer.readUnsignedMediumLE().toUInt())
            )
        }
        records.add(record)
    }
    return records
}

private fun compressRecords(queue: MutableList<UInt>): MutableList<Record> {
    val records = mutableListOf<Record>()
    val iterator = queue.iterator()
    var start = iterator.next()
    var end = start
    while(iterator.hasNext()) {
        val next = iterator.next()
        if(end - next > 1u) {
            records.add(if(start != end) Record.Range(UMediumLE(start), UMediumLE(end)) else Record.Single(UMediumLE(start)))
            start = next
            end = next
        } else {
            end = next
        }
    }
    records.add(if(start != end) Record.Range(UMediumLE(start), UMediumLE(end)) else Record.Single(UMediumLE(start)))
    return records
}