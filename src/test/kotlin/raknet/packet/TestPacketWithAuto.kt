package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import raknet.encode

class TestPacketWithAuto(
    private val testLong: Long,
    private val testString: String,
    private val testRandom: Int,
): DataPacket(0xCD) {

    override fun decode(buffer: ByteBuf) {}

    override fun encode(): ByteBuf {
        val buffer: ByteBuf = Unpooled.buffer()
        encodeOrder().forEach { it.encode(buffer) }
        return buffer
    }

    override fun encodeOrder(): Array<Any> = arrayOf(testLong, testString, testRandom)
}