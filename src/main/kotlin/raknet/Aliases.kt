package raknet

import io.netty.buffer.ByteBuf

typealias IntLE = Int
fun IntLE.encode(buffer: ByteBuf) = buffer.writeIntLE(this)

typealias UIntLE = UInt
fun UIntLE.encode(buffer: ByteBuf) = buffer.writeIntLE(this.toInt())

typealias LongLE = Long
fun LongLE.encode(buffer: ByteBuf) = buffer.writeLongLE(this)

typealias ULongLE = ULong
fun ULongLE.encode(buffer: ByteBuf) = buffer.writeLongLE(this.toLong())

typealias FloatLE = Float
fun FloatLE.encode(buffer: ByteBuf) = buffer.writeFloatLE(this)

typealias DoubleLE = Double
fun DoubleLE.encode(buffer: ByteBuf) = buffer.writeDoubleLE(this)