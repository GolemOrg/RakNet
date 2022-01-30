package raknet.codec

import io.netty.buffer.ByteBuf
import raknet.readToByteArray
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.experimental.and
import kotlin.experimental.inv

interface Codable {

    fun encode(buffer: ByteBuf)

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

fun ByteArray.flip(): ByteArray {
    val result = ByteArray(this.size)
    for (i in 0 until this.size) {
        result[i] = (this[i] and 0xFF.toByte()).inv()
    }
    return result
}

fun Any?.encode(buffer: ByteBuf) {
    when (this) {
        is Byte -> buffer.writeByte(this.toInt())
        is Boolean -> buffer.writeBoolean(this)
        is Short -> buffer.writeShort(this.toInt())
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
        is InetSocketAddress -> {
            when (val inner: InetAddress = this.address) {
                is Inet4Address -> {
                    buffer.writeByte(4) //IPv4
                    buffer.writeBytes(inner.address.flip())
                    buffer.writeShort(this.port)
                }
                is Inet6Address -> {
                    buffer.writeByte(6) // IPv6
                    buffer.writeShort(10) // AF_INET6
                    buffer.writeShort(this.port)
                    buffer.writeInt(0) // Flow info
                    buffer.writeBytes(inner.address)
                    buffer.writeInt(inner.scopeId)
                }
            }
        }
        is Array<*> -> this.forEach { it.encode(buffer) }
        // Don't worry about other cases
        else -> {
            val type = this?.javaClass!!.simpleName ?: "null"
            println("Encountered unknown type: $type when encoding value to buffer")
        }
    }
}
