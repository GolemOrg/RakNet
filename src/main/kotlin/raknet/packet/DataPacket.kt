package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import raknet.codec.encode
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

abstract class DataPacket(private val id: Short) : Packet {

    override fun encode(): ByteArray {
        var buffer = Unpooled.buffer()
        val reflected = this::class
        // This might be a little heavy to do every time, but we'll have to do more performance testing to see if it's worth it
        reflected.primaryConstructor?.parameters?.forEachIndexed { _, parameter ->
            val value = reflected.memberProperties.find { it.name == parameter.name }?.getter?.call(this)
            buffer = value.encode(buffer)
        }
        return buffer.array().clone()
    }

    abstract override fun decode(buffer: ByteBuf)

    fun prepare(): ByteBuf {
        return Unpooled.buffer()
            .writeByte(id.toInt())
            .writeBytes(encode())
    }


    override fun toString(): String {
        return "DataPacket(id=$id)"
    }

}

fun ByteBuf.readToByteArray(length: Int): ByteArray {
    val bytes = ByteArray(length)
    readBytes(bytes)
    return bytes
}