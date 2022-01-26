package raknet

val MAGIC: ByteArray = byteArrayOf(
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