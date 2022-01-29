package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import raknet.codec.encode

abstract class DataPacket(private val id: Short) : Packet {

    override fun encode(): ByteArray {
        val buffer = Unpooled.buffer()
        encodeOrder().forEach { it.encode(buffer) }
        return buffer.array().clone()
    }

    override fun decode(buffer: ByteBuf) {
        // We don't use decode at the moment as we use Packet.from() as a way to decode the packet
        // It may be worth a look at using decode in the future
    }

    abstract fun encodeOrder(): Array<Any>

    fun prepare(): ByteBuf {
        return Unpooled.buffer()
            .writeByte(id.toInt())
            .writeBytes(encode())
    }

    override fun toString(): String {
        return "DataPacket(id=$id)"
    }

}