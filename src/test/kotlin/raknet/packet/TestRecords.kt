package raknet.packet

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestRecords {

    @Test
    fun testCompress() {
        val uncompressed = mutableListOf(1, 3, 4, 5, 6, 8, 9)
        assertEquals(mutableListOf(Record(1.toUInt()), Record(3.toUInt(), 6.toUInt()), Record(8.toUInt(), 9.toUInt())), Record.fromList(uncompressed))
    }
}