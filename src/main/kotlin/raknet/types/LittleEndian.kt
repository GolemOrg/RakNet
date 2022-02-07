package raknet.types

import io.netty.buffer.ByteBuf
import raknet.codec.Decodable
import raknet.codec.Encodable

@JvmInline
value class IntLE(private val value: Int): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeIntLE(value) }
    override fun decode(buffer: ByteBuf): IntLE = IntLE(buffer.readIntLE())
}

@JvmInline
value class UIntLE(private val value: UInt): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeIntLE(value.toInt()) }
    override fun decode(buffer: ByteBuf): IntLE = IntLE(buffer.readIntLE())
}

@JvmInline
value class UInt24LE(private val value: UInt): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeMediumLE(value.toInt()) }
    override fun decode(buffer: ByteBuf): UInt24LE = UInt24LE(buffer.readUnsignedMediumLE().toUInt())
}

@JvmInline
value class LongLE(private val value: Long): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeLongLE(value) }
    override fun decode(buffer: ByteBuf): LongLE = LongLE(buffer.readLongLE())
}

@JvmInline
value class ULongLE(private val value: ULong): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeLongLE(value.toLong()) }
    override fun decode(buffer: ByteBuf): LongLE = LongLE(buffer.readLongLE())
}

@JvmInline
value class FloatLE(private val value: Float): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeFloatLE(value) }
    override fun decode(buffer: ByteBuf): FloatLE = FloatLE(buffer.readFloatLE())
}

@JvmInline
value class DoubleLE(private val value: Double): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeDoubleLE(value) }
    override fun decode(buffer: ByteBuf): DoubleLE = DoubleLE(buffer.readDoubleLE())
}