package raknet.codec

import io.netty.buffer.ByteBuf
import raknet.readToByteArray

interface Codable {

    fun encode(buffer: ByteBuf): Unit

    fun decode(buffer: ByteBuf): Any

}

fun Any?.decode(buffer: ByteBuf): Any? {
    return when(this) {
        is Byte -> buffer.readByte()
        is Short -> buffer.readShort()
        is Int -> buffer.readInt()
        is Long -> buffer.readLong()
        is Float -> buffer.readFloat()
        is Double -> buffer.readDouble()
        is Char -> buffer.readChar()
        is Boolean -> buffer.readBoolean()
        is String -> {
            val length = buffer.readShort()
            return buffer.readCharSequence(length.toInt(), Charsets.UTF_8).toString()
        }
        is ByteArray -> buffer.readToByteArray(this.size)
        is Codable -> this.decode(buffer)
        else -> null
    }
}

fun Any?.encode(buffer: ByteBuf) {
    when (this) {
        is Byte -> buffer.writeByte(this as Int)
        is Short -> buffer.writeShort(this as Int)
        is Int -> buffer.writeInt(this)
        is Long -> buffer.writeLong(this)
        is Float -> buffer.writeFloat(this)
        is Double -> buffer.writeDouble(this)
        is String -> {
            buffer.writeShort(this.length)
            buffer.writeCharSequence(this, Charsets.UTF_8)
        }
        is ByteArray -> buffer.writeBytes(this)
        is Codable -> this.encode(buffer)
        // Don't worry about other cases
        else -> {}
    }
}