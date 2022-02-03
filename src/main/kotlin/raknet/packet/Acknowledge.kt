package raknet.packet

import io.netty.buffer.ByteBuf
import raknet.UIntLE
import raknet.codec.Codable
import kotlin.system.exitProcess

data class Record(val sequenceNumber: UIntLE, val endSequenceNumber: UIntLE? = null): Codable {

    override fun encode(buffer: ByteBuf) {
        val single = endSequenceNumber == null
        buffer.writeBoolean(single)
        buffer.writeMediumLE(sequenceNumber.toInt())
        if(!single) buffer.writeMediumLE(endSequenceNumber!!.toInt())
    }

    override fun decode(buffer: ByteBuf): Record {
        val single = buffer.readBoolean()
        return Record(
            sequenceNumber = buffer.readUnsignedMediumLE().toUInt(),
            endSequenceNumber = if(!single) buffer.readUnsignedMediumLE().toUInt() else null
        )
    }

    companion object {
        fun from(buffer: ByteBuf): Record {
            val single = buffer.readBoolean()
            return Record(
                sequenceNumber = buffer.readUnsignedMediumLE().toUInt(),
                endSequenceNumber = if(!single) buffer.readUnsignedMediumLE().toUInt() else null
            )
        }

        fun fromList(list: List<Int>): MutableList<Record> {
            val records = mutableListOf<Record>()
            val iterator = list.sorted().iterator()

            var start = iterator.next()
            var end = start
            while(iterator.hasNext()) {
                val current = iterator.next()
                if(current - end <= 1) {
                    end = current
                    continue
                }
                records.add(Record(
                    sequenceNumber = start.toUInt(),
                    endSequenceNumber = if (end == start) null else end.toUInt()
                ))

                start = current
                end = current
            }
            records.add(Record(sequenceNumber = start.toUInt(), endSequenceNumber = if (end == start) null else end.toUInt()))
            return records
        }
    }

    override fun toString() = "Record(sequenceNumber=$sequenceNumber, endSequenceNumber=$endSequenceNumber)"
}

sealed class Base(id: Int, val records: MutableList<Record>): ConnectedPacket(id) {
    override fun encodeOrder(): Array<Any> = arrayOf(records.size.toShort(), records)
}

class Acknowledge(records: MutableList<Record>): Base(PacketType.ACK.id(), records) {
    override fun toString(): String = "Acknowledge(records=${records.joinToString()})"

    companion object {
        fun from(buffer: ByteBuf): Acknowledge {
            val records = mutableListOf<Record>()
            for(i in 0 until buffer.readShort()) records.add(Record.from(buffer))
            return Acknowledge(records)
        }
    }
}
class NAcknowledge(records: MutableList<Record>): Base(PacketType.NACK.id(), records) {
    override fun toString(): String = "NAcknowledge(records=${records.joinToString()})"

    companion object {
        fun from(buffer: ByteBuf): NAcknowledge {
            val records = mutableListOf<Record>()
            for(i in 0 until buffer.readShort()) records.add(Record.from(buffer))
            return NAcknowledge(records)
        }
    }
}