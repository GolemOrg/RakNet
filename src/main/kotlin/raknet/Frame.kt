package raknet

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import raknet.enums.Flag
import raknet.enums.ReliabilityType
import kotlin.experimental.and


data class Order(val orderIndex: UIntLE, val orderChannel: Byte)
data class Fragment(val compoundSize: Int, val compoundId: Short, val index: Int)

class Frame(
    val reliability: ReliabilityType,
    val length: UShort,
    val reliableFrameIndex: UIntLE? = null,
    val sequencedFrameIndex: UIntLE? = null,
    val order: Order? = null,
    val fragment: Fragment? = null,
    val body: ByteBuf
){

    fun split(): Boolean {
        return fragment != null
    }

    fun encode(): ByteBuf {
        val buffer = ByteBufAllocator.DEFAULT.ioBuffer()

        buffer.writeByte(reliability.flags().toInt())
        buffer.writeShort(length.toInt().shl(BODY_LENGTH_SHIFT))

        if(reliability.reliable) buffer.writeMediumLE(reliableFrameIndex!!.toInt())
        if(reliability.sequenced) buffer.writeMediumLE(sequencedFrameIndex!!.toInt())
        if(reliability.sequenced || reliability.ordered) {
            buffer.writeMediumLE(order!!.orderIndex.toInt())
            buffer.writeByte(order.orderChannel.toInt())
        }

        if(split()) {
            buffer.writeInt(fragment!!.compoundSize)
            buffer.writeShort(fragment.compoundId.toInt())
            buffer.writeByte(fragment.index)
        }
        buffer.writeBytes(body)

        return buffer
    }

    companion object {

        const val BODY_LENGTH_SHIFT = 3

        fun from(buffer: ByteBuf): Frame {
            val flags: Byte = buffer.readByte()
            val reliability = ReliabilityType.fromRaw(flags)

            val split = flags.and(Flag.PACKET_PAIR.id().toByte()) != 0.toByte()

            // The length is ceil(short) / 8
            val length = (buffer.readUnsignedShort() + 7).shr(BODY_LENGTH_SHIFT)
            if(length <= 0) {
                throw RuntimeException("Payload length must be greater than 0")
            }

            return Frame(
                reliability,
                length.toUShort(),
                if (reliability.reliable) buffer.readUnsignedMediumLE().toUInt() else null,
                if (reliability.sequenced) buffer.readUnsignedMediumLE().toUInt() else null,
                if (reliability.sequenced || reliability.ordered) Order(buffer.readUnsignedMediumLE().toUInt(), buffer.readUnsignedByte().toByte()) else null,
                if(split) Fragment(buffer.readInt(), buffer.readShort(), buffer.readInt()) else null,
                buffer.readSlice(length)
            )
        }
    }

    override fun toString(): String = "Frame(reliabilityType=$reliability, length=$length, reliableFrameIndex=$reliableFrameIndex, sequencedFrameIndex=$sequencedFrameIndex, order=$order, fragment=$fragment, body=Body(${body.readableBytes()}))"

}