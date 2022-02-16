package org.golem.raknet.enums

import org.junit.Test
import kotlin.test.assertEquals

internal class ReliabilityTest {

    @Test
    fun testDecodeFlags() {
        val flags = 0b011
        assertEquals(Reliability.RELIABLE_ORDERED, Reliability.from(flags))
    }

    @Test
    fun testDecodeRawFlags() {
        val flags = 0b0110_0000
        assertEquals(Reliability.RELIABLE_ORDERED, Reliability.fromRaw(flags))
    }

    @Test
    fun testEncodeFlags() {
        val unreliable = Reliability.UNRELIABLE.toByte()
        assertEquals(unreliable, 0b000.toByte())

        val unreliableSequenced = Reliability.UNRELIABLE_SEQUENCED.toByte()
        assertEquals(unreliableSequenced, 0b001.toByte())

        val reliable = Reliability.RELIABLE.toByte()
        assertEquals(reliable, 0b010.toByte())

        val reliableOrdered = Reliability.RELIABLE_ORDERED.toByte()
        assertEquals(reliableOrdered, 0b011.toByte())

        val reliableSequenced = Reliability.RELIABLE_SEQUENCED.toByte()
        assertEquals(reliableSequenced, 0b100.toByte())
    }
}