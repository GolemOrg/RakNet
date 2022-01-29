package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import raknet.codec.encode

class TestPacketWithAuto(val testLong: Long, val testString: String, val testRandom: Int): DataPacket(0xCD) {

    override fun decode(buffer: ByteBuf) {}

    override fun encode(): ByteArray {
        val buffer: ByteBuf = Unpooled.buffer()
        for (field in encodeOrder()) {
            field.encode(buffer)
        }
        return buffer.array().clone()
    }

    fun encodeOrder(): Array<Any> {
        return arrayOf(testLong, testString, testRandom)
    }
}