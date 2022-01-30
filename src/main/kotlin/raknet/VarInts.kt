package raknet

import io.netty.buffer.ByteBuf
import raknet.codec.Codable

const val SHIFT_SIZE = 7
const val MAX_BYTES_SIZE = 4
const val STEP_SIZE = SHIFT_SIZE * MAX_BYTES_SIZE

class VarInt(private val value: Int): Number(), Codable {

    override fun encode(buffer: ByteBuf) {}

    override fun decode(buffer: ByteBuf): VarInt = VarInt(0)

    override fun toByte(): Byte = value.toByte()

    override fun toChar(): Char = value.toChar()

    override fun toDouble(): Double = value.toDouble()

    override fun toFloat(): Float = value.toFloat()

    override fun toInt(): Int = value

    override fun toLong(): Long = value.toLong()

    override fun toShort(): Short = value.toShort()

}

class UVarInt(private val value: UInt): Number(), Codable {

    override fun encode(buffer: ByteBuf) {}

    override fun decode(buffer: ByteBuf): UVarInt = UVarInt(0.toUInt())

    override fun toByte(): Byte = value.toByte()

    override fun toChar(): Char = value.toInt().toChar()

    override fun toDouble(): Double = value.toDouble()

    override fun toFloat(): Float = value.toFloat()

    override fun toInt(): Int = value.toInt()

    override fun toLong(): Long = value.toLong()

    override fun toShort(): Short = value.toShort()

}