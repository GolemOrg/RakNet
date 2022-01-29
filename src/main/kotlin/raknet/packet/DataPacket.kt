package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

abstract class DataPacket(private val id: Short) : Packet {

    override fun encode(): ByteArray {
        return ByteArray(0)
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