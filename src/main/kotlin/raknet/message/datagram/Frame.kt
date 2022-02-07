package raknet.message.datagram

import io.netty.buffer.ByteBuf
import raknet.codec.Encodable
import raknet.encode
import raknet.enums.Flags
import raknet.enums.Reliability
import raknet.types.UInt24LE

sealed class Frame(
    val reliability: Reliability,
    val fragment: Fragment?,
    val body: ByteBuf
): Encodable {
    class Unreliable(fragment: Fragment?, body: ByteBuf) : Frame(Reliability.UNRELIABLE, fragment, body) {
        override fun encodeOrder(): Array<Any> = arrayOf()
        override fun toString(): String = "Unreliable(fragment=$fragment, body=ByteBuf(${body.readableBytes()}))"
    }
    class UnreliableSequenced(
        val sequenceIndex: UInt24LE,
        fragment: Fragment?,
        body: ByteBuf
    ) : Frame(Reliability.UNRELIABLE_SEQUENCED, fragment, body) {
        override fun encodeOrder(): Array<Any> = arrayOf(sequenceIndex)
        override fun toString(): String = "UnreliableSequenced(sequenceIndex=$sequenceIndex, fragment=$fragment, body=ByteBuf(${body.readableBytes()}))"
    }
    class Reliable(
        val messageIndex: UInt24LE,
        fragment: Fragment?,
        body: ByteBuf
    ) : Frame(Reliability.RELIABLE, fragment, body) {
        override fun encodeOrder(): Array<Any> = arrayOf(messageIndex)
        override fun toString(): String = "Reliable(messageIndex=$messageIndex, fragment=$fragment, body=ByteBuf(${body.readableBytes()}))"
    }
    class ReliableOrdered(
        val messageIndex: UInt24LE,
        val order: Order,
        fragment: Fragment?,
        body: ByteBuf
    ): Frame(Reliability.RELIABLE_ORDERED, fragment, body) {
        override fun encodeOrder(): Array<Any> = arrayOf(messageIndex, order)
        override fun toString(): String = "ReliableOrdered(messageIndex=$messageIndex, order=$order, fragment=$fragment, body=ByteBuf(${body.readableBytes()}))"
    }

    override fun encode(buffer: ByteBuf) {
        var flags = reliability.toRaw()
        if(fragment != null) flags = flags or Flags.PACKET_PAIR.id()
        buffer.writeByte(flags)
        buffer.writeShort(body.readableBytes() + 7 shl BODY_LENGTH_SHIFT)
        encodeOrder().forEach { it.encode(buffer) }
        buffer.writeBytes(body)
    }

    abstract fun encodeOrder(): Array<Any>
    abstract override fun toString(): String

    companion object {

        const val BODY_LENGTH_SHIFT = 3

        fun from(buffer: ByteBuf): Frame {
            val flags = buffer.readUnsignedByte().toInt()
            val reliability = Reliability.fromRaw(flags)
            val hasFragment = flags and Flags.PACKET_PAIR.id() != 0
            val bodyLength = buffer.readUnsignedShort() shr BODY_LENGTH_SHIFT
            return when(reliability) {
                Reliability.UNRELIABLE -> Unreliable(
                    fragment = if(hasFragment) Fragment(buffer.readInt(), buffer.readShort(), buffer.readInt()) else null,
                    body = buffer.readBytes(bodyLength)
                )
                Reliability.UNRELIABLE_SEQUENCED -> UnreliableSequenced(
                    sequenceIndex = UInt24LE(buffer.readUnsignedMediumLE().toUInt()),
                    fragment = if(hasFragment) Fragment(buffer.readInt(), buffer.readShort(), buffer.readInt()) else null,
                    body = buffer.readBytes(bodyLength)
                )
                Reliability.RELIABLE -> Reliable(
                    messageIndex =  UInt24LE(buffer.readUnsignedMediumLE().toUInt()),
                    fragment = if(hasFragment) Fragment(buffer.readInt(), buffer.readShort(), buffer.readInt()) else null,
                    body = buffer.readBytes(bodyLength)
                )
                Reliability.RELIABLE_ORDERED -> ReliableOrdered(
                    messageIndex =  UInt24LE(buffer.readUnsignedMediumLE().toUInt()),
                    order = Order(
                        index = UInt24LE(buffer.readUnsignedMediumLE().toUInt()),
                        channel = buffer.readUnsignedByte().toByte()
                    ),
                    fragment = if(hasFragment) Fragment(buffer.readInt(), buffer.readShort(), buffer.readInt()) else null,
                    body = buffer.readBytes(bodyLength)
                )
                else -> throw IllegalArgumentException("Unknown reliability: $reliability")
            }
        }

    }
}