package org.golem.raknet.message.datagram

import io.netty.buffer.ByteBuf
import org.golem.netty.codec.OrderedEncodable
import org.golem.netty.codec.encode
import org.golem.raknet.enums.Flags
import org.golem.raknet.enums.Reliability
import org.golem.netty.types.UMediumLE

sealed class Frame(
    private val reliability: Reliability,
    val fragment: Fragment?,
    val body: ByteBuf
): OrderedEncodable {
    class Unreliable(fragment: Fragment?, body: ByteBuf) : Frame(Reliability.UNRELIABLE, fragment, body) {
        override fun encodeOrder(): Array<Any> = arrayOf()
        override fun toString(): String = "Unreliable(fragment=$fragment, body=ByteBuf(${body.readableBytes()}))"
    }
    class UnreliableSequenced(
        val sequenceIndex: UMediumLE,
        fragment: Fragment?,
        body: ByteBuf
    ) : Frame(Reliability.UNRELIABLE_SEQUENCED, fragment, body) {
        override fun encodeOrder(): Array<Any> = arrayOf(sequenceIndex)
        override fun toString(): String = "UnreliableSequenced(sequenceIndex=$sequenceIndex, fragment=$fragment, body=ByteBuf(${body.readableBytes()}))"
    }
    class Reliable(
        val messageIndex: UMediumLE,
        fragment: Fragment?,
        body: ByteBuf
    ) : Frame(Reliability.RELIABLE, fragment, body) {
        override fun encodeOrder(): Array<Any> = arrayOf(messageIndex)
        override fun toString(): String = "Reliable(messageIndex=$messageIndex, fragment=$fragment, body=ByteBuf(${body.readableBytes()}))"
    }
    class ReliableOrdered(
        val messageIndex: UMediumLE,
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
        buffer.writeShort(body.readableBytes() shl BODY_LENGTH_SHIFT)
        encodeOrder().encode(buffer)
        buffer.writeBytes(body)
    }

    abstract override fun toString(): String

    companion object {

        const val BODY_LENGTH_SHIFT = 3

        fun from(buffer: ByteBuf): Frame {
            val flags = buffer.readUnsignedByte().toInt()
            val reliability = Reliability.fromRaw(flags)
            val hasFragment = flags and Flags.PACKET_PAIR.id() != 0
            val bodyLength = buffer.readUnsignedShort() + 7 shr BODY_LENGTH_SHIFT
            return when(reliability) {
                Reliability.UNRELIABLE -> Unreliable(
                    fragment = if(hasFragment) Fragment(buffer.readInt(), buffer.readShort(), buffer.readInt()) else null,
                    body = buffer.readBytes(bodyLength)
                )
                Reliability.UNRELIABLE_SEQUENCED -> UnreliableSequenced(
                    sequenceIndex = UMediumLE(buffer.readUnsignedMediumLE().toUInt()),
                    fragment = if(hasFragment) Fragment(buffer.readInt(), buffer.readShort(), buffer.readInt()) else null,
                    body = buffer.readBytes(bodyLength)
                )
                Reliability.RELIABLE -> Reliable(
                    messageIndex =  UMediumLE(buffer.readUnsignedMediumLE().toUInt()),
                    fragment = if(hasFragment) Fragment(buffer.readInt(), buffer.readShort(), buffer.readInt()) else null,
                    body = buffer.readBytes(bodyLength)
                )
                Reliability.RELIABLE_ORDERED -> ReliableOrdered(
                    messageIndex =  UMediumLE(buffer.readUnsignedMediumLE().toUInt()),
                    order = Order(
                        index = UMediumLE(buffer.readUnsignedMediumLE().toUInt()),
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