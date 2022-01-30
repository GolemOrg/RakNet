package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import raknet.codec.encode

abstract class DataPacket(val id: Short) : Packet {

    override fun encode(): ByteBuf {
        val buffer = ByteBufAllocator.DEFAULT.ioBuffer()
        encodeOrder().forEach { it.encode(buffer) }
        return buffer
    }

    override fun decode(buffer: ByteBuf) {
        // We don't use decode at the moment as we use Packet.from() as a way to decode the packet
        // It may be worth a look at using decode in the future
    }

    abstract fun encodeOrder(): Array<Any>

    fun prepare(): ByteBuf {
        val encoded = encode()
        try {
            return ByteBufAllocator.DEFAULT.ioBuffer()
                .writeByte(id.toInt())
                .writeBytes(encoded)
        } finally {
            encoded.release()
        }

    }

    override fun toString(): String {
        return "DataPacket(id=$id)"
    }

}