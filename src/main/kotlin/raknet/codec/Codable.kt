package raknet.codec

import io.netty.buffer.ByteBuf

interface Codable {

    fun encode(buffer: ByteBuf): ByteBuf

    fun decode(buffer: ByteBuf): Any

}

fun Any?.encode(buffer: ByteBuf): ByteBuf {
    return when (this) {
        is Byte -> buffer.writeByte(this as Int)
        is Short -> buffer.writeShort(this as Int)
        is Int -> buffer.writeInt(this)
        is Long -> buffer.writeLong(this)
        is Float -> buffer.writeFloat(this)
        is Double -> buffer.writeDouble(this)
        is String -> {
            buffer.writeShort(this.length)
            buffer.writeCharSequence(this, Charsets.UTF_8)
            return buffer
        }
        is ByteArray -> buffer.writeBytes(this)
        is Codable -> this.encode(buffer)
        // Just return the buffer as is
        else -> buffer
    }
}