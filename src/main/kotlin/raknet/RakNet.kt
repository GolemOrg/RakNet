package raknet

import kotlin.collections.ArrayList


object Magic {

    val BYTES = byteArrayOf(
        0x00.toByte(),
        0xff.toByte(),
        0xff.toByte(),
        0x00.toByte(),
        0xfe.toByte(),
        0xfe.toByte(),
        0xfe.toByte(),
        0xfe.toByte(),
        0xfd.toByte(),
        0xfd.toByte(),
        0xfd.toByte(),
        0xfd.toByte(),
        0x12.toByte(),
        0x34.toByte(),
        0x56.toByte(),
        0x78.toByte()
    )

    fun verify(bytes: ByteArray): Magic {
        val magic = Magic
        require(bytes.contentEquals(BYTES)) { "Input bytes must match" }
        return magic
    }
}

data class Identifier(val values: ArrayList<Any>) {

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(HEADER)
        builder.append(SEPARATOR)
        for (value in values) {
            builder.append(value)
            builder.append(SEPARATOR)
        }
        return builder.toString()
    }

    companion object {
        const val HEADER: String = "MCPE"
        const val SEPARATOR: Char = ';'
    }

}