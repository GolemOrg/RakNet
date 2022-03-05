package org.golem.raknet.types

import io.netty.buffer.ByteBuf
import org.golem.raknet.codec.Decodable
import org.golem.raknet.codec.Encodable

@JvmInline
value class IntLE(private val value: Int): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeIntLE(value) }
    override fun decode(buffer: ByteBuf): IntLE = IntLE(buffer.readIntLE())
    fun toInt(): Int = value
    fun toUInt(): UInt = value.toUInt()
    override fun toString(): String = value.toString()
}

fun Int.toIntLE() = IntLE(this)
fun UInt.toIntLE() = IntLE(this.toInt())

@JvmInline
value class UIntLE(private val value: UInt): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeIntLE(value.toInt()) }
    override fun decode(buffer: ByteBuf): IntLE = IntLE(buffer.readIntLE())
    fun toInt(): Int = value.toInt()
    fun toUInt(): UInt = value
    override fun toString(): String = value.toString()
}

fun UInt.toUIntLE() = UIntLE(this)
fun Int.toUIntLE() = UIntLE(this.toUInt())

@JvmInline
value class UInt24LE(private val value: UInt): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeMediumLE(value.toInt()) }
    override fun decode(buffer: ByteBuf): UInt24LE = UInt24LE(buffer.readUnsignedMediumLE().toUInt())
    fun toInt(): Int = value.toInt()
    fun toUInt(): UInt = value
    override fun toString(): String = value.toString()
}

fun UInt.toUInt24LE() = UInt24LE(this)
fun Int.toUInt24LE() = UInt24LE(this.toUInt())

@JvmInline
value class LongLE(private val value: Long): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeLongLE(value) }
    override fun decode(buffer: ByteBuf): LongLE = LongLE(buffer.readLongLE())
    fun toLong(): Long = value
    fun toULong(): ULong = value.toULong()
    override fun toString(): String = value.toString()
}

fun Long.toLongLE() = LongLE(this)
fun ULong.toLongLE() = LongLE(this.toLong())

@JvmInline
value class ULongLE(private val value: ULong): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeLongLE(value.toLong()) }
    override fun decode(buffer: ByteBuf): LongLE = LongLE(buffer.readLongLE())
    fun toLong(): Long = value.toLong()
    fun toULong(): ULong = value
    override fun toString(): String = value.toString()
}

fun ULong.toULongLE() = ULongLE(this)
fun Long.toULongLE() = ULongLE(this.toULong())

@JvmInline
value class FloatLE(private val value: Float): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeFloatLE(value) }
    override fun decode(buffer: ByteBuf): FloatLE = FloatLE(buffer.readFloatLE())
    fun toFloat(): Float = value
    override fun toString(): String = value.toString()
}

fun Float.toFloatLE() = FloatLE(this)
fun Double.toFloatLE() = FloatLE(this.toFloat())

@JvmInline
value class DoubleLE(private val value: Double): Encodable, Decodable {
    override fun encode(buffer: ByteBuf) { buffer.writeDoubleLE(value) }
    override fun decode(buffer: ByteBuf): DoubleLE = DoubleLE(buffer.readDoubleLE())
    fun toDouble(): Double = value
    override fun toString(): String = value.toString()
}

fun Double.toDoubleLE() = DoubleLE(this)
fun Float.toDoubleLE() = DoubleLE(this.toDouble())