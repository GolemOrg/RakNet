package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

class TestPacketWithoutAuto(val testLong: Long, val testString: String, val testRandom: Int): DataPacket(0xEF) {

    override fun encodeOrder(): Array<Any> {
        return arrayOf(testLong, testString, testRandom) // Unused as we override encode
    }

    override fun encode(): ByteBuf {
        val buffer: ByteBuf = Unpooled.buffer()
        buffer.writeLong(testLong)
        buffer.writeShort(testString.length)
        buffer.writeCharSequence(testString, Charsets.UTF_8)
        buffer.writeInt(testRandom)
        return buffer
    }
}