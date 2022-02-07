package raknet

import io.netty.buffer.ByteBuf
import raknet.codec.Codable

@JvmInline
value class IntLE(private val value: Int): Codable {
    override fun encode(buffer: ByteBuf) { buffer.writeIntLE(value) }
    override fun decode(buffer: ByteBuf): IntLE = IntLE(buffer.readIntLE())
}

@JvmInline
value class UIntLE(private val value: UInt): Codable {
    override fun encode(buffer: ByteBuf) { buffer.writeIntLE(value.toInt()) }
    override fun decode(buffer: ByteBuf): IntLE = IntLE(buffer.readIntLE())
}

@JvmInline
value class LongLE(private val value: Long): Codable {
    override fun encode(buffer: ByteBuf) { buffer.writeLongLE(value) }
    override fun decode(buffer: ByteBuf): LongLE = LongLE(buffer.readLongLE())
}

@JvmInline
value class ULongLE(private val value: ULong): Codable {
    override fun encode(buffer: ByteBuf) { buffer.writeLongLE(value.toLong()) }
    override fun decode(buffer: ByteBuf): LongLE = LongLE(buffer.readLongLE())
}

@JvmInline
value class FloatLE(private val value: Float): Codable {
    override fun encode(buffer: ByteBuf) { buffer.writeFloatLE(value) }
    override fun decode(buffer: ByteBuf): FloatLE = FloatLE(buffer.readFloatLE())
}

@JvmInline
value class DoubleLE(private val value: Double): Codable {
    override fun encode(buffer: ByteBuf) { buffer.writeDoubleLE(value) }
    override fun decode(buffer: ByteBuf): DoubleLE = DoubleLE(buffer.readDoubleLE())
}