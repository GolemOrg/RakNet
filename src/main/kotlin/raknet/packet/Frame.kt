package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import raknet.UIntLE
import raknet.codec.Codable
import raknet.enums.Flag
import raknet.enums.Reliability


data class Order(val orderIndex: UIntLE, val orderChannel: Byte): Codable {
    override fun encode(buffer: ByteBuf) {
        buffer.writeMediumLE(orderIndex.toInt())
        buffer.writeByte(orderChannel.toInt())
    }

    override fun decode(buffer: ByteBuf) = Order(orderIndex = buffer.readUnsignedMediumLE().toUInt(), orderChannel = buffer.readUnsignedByte().toByte())

    companion object { fun from(buffer: ByteBuf) = Order(buffer.readUnsignedMediumLE().toUInt(), buffer.readUnsignedByte().toByte()) }
}

data class Fragment(val compoundSize: Int, val compoundId: Short, val index: Int): Codable {
    override fun encode(buffer: ByteBuf) {
        buffer.writeInt(compoundSize)
        buffer.writeShort(compoundId.toInt())
        buffer.writeInt(index)
    }

    override fun decode(buffer: ByteBuf) = Fragment(compoundSize = buffer.readInt(), compoundId = buffer.readShort(), index = buffer.readInt())

    companion object { fun from(buffer: ByteBuf) = Fragment(compoundSize = buffer.readInt(), compoundId = buffer.readShort(), index = buffer.readInt()) }

}

class Frame(
    val reliability: Reliability,
    val reliableFrameIndex: UIntLE? = null,
    val sequencedFrameIndex: UIntLE? = null,
    val order: Order? = null,
    val fragment: Fragment? = null,
    val body: ByteBuf,
) {

    fun encode(): ByteBuf {
        val buffer = ByteBufAllocator.DEFAULT.ioBuffer()
        var flags = reliability.toRaw()
        if(fragment != null) flags = flags or Flag.PACKET_PAIR.id()
        buffer.writeByte(flags)
        buffer.writeShort(body.readableBytes() shl BODY_LENGTH_SHIFT)

        if(reliableFrameIndex != null) buffer.writeMediumLE(reliableFrameIndex.toInt())
        if(sequencedFrameIndex != null) buffer.writeMediumLE(sequencedFrameIndex.toInt())
        order?.encode(buffer)
        fragment?.encode(buffer)
        buffer.writeBytes(body, body.readerIndex(), body.readableBytes())
        return buffer
    }

    companion object {

        private const val BODY_LENGTH_SHIFT: Int = 3

        fun from(buffer: ByteBuf): Frame {
            val flags: Int = buffer.readUnsignedByte().toInt()
            val reliability = Reliability.fromRaw(flags)

            val split = flags and Flag.PACKET_PAIR.id() != 0

            // The length is ceil(short) / 8
            val length = (buffer.readUnsignedShort() + 7) ushr BODY_LENGTH_SHIFT
            if(length <= 0) {
                throw RuntimeException("Payload length must be greater than 0")
            }

            return Frame(
                reliability,
                if (reliability.reliable()) buffer.readUnsignedMediumLE().toUInt() else null,
                if (reliability.sequenced()) buffer.readUnsignedMediumLE().toUInt() else null,
                if (reliability.sequenced() || reliability.ordered()) Order.from(buffer) else null,
                if(split) Fragment.from(buffer) else null,
                buffer.readSlice(length)
            )
        }
    }

    override fun toString(): String = "Frame(reliabilityType=$reliability, reliableFrameIndex=$reliableFrameIndex, sequencedFrameIndex=$sequencedFrameIndex, order=$order, fragment=$fragment, body=Body(${body.readableBytes()}))"

}