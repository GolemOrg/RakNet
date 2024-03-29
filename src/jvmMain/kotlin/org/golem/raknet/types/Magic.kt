package org.golem.raknet.types

import io.netty.buffer.ByteBuf
import org.golem.netty.codec.Decodable
import org.golem.netty.codec.Encodable
import org.golem.netty.readToByteArray

object Magic : Encodable, Decodable {

    private val bytes = byteArrayOf(
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

    private val size get() = bytes.size

    private fun verify(bytes: ByteArray): Magic {
        require(bytes.contentEquals(Magic.bytes)) { "Input bytes must match" }
        return Magic
    }

    override fun encode(buffer: ByteBuf) {
        buffer.writeMagic()
    }

    override fun decode(buffer: ByteBuf) = buffer.readMagic()

    fun ByteBuf.writeMagic(): ByteBuf = writeBytes(bytes)
    fun ByteBuf.readMagic(): Magic = verify(readToByteArray(size))

    override fun toString(): String = "Magic()"
}