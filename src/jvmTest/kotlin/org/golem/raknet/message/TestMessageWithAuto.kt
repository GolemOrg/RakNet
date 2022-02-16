package org.golem.raknet.message

import io.netty.buffer.ByteBuf
import org.golem.raknet.encode

class TestMessageWithAuto(
    private val testLong: Long,
    private val testString: String,
    private val testRandom: Int,
): DataMessage(0xCD) {

    override fun decode(buffer: ByteBuf) {}

    override fun encode(buffer: ByteBuf) {
        encodeOrder().forEach { it.encode(buffer) }
    }

    override fun encodeOrder(): Array<Any> = arrayOf(testLong, testString, testRandom)
}