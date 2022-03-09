package org.golem.raknet.types

import io.netty.buffer.ByteBuf
import org.golem.netty.codec.Encodable

@JvmInline
value class RakString(val value: String): Encodable {
    override fun encode(buffer: ByteBuf) {
        buffer.writeRakString(value)
    }

    override fun toString(): String = value
}

fun ByteBuf.readRakString(): String = readCharSequence(readUnsignedShort(), Charsets.UTF_8).toString()
fun ByteBuf.writeRakString(value: String) {
    this.writeShort(value.length)
    this.writeCharSequence(value, Charsets.UTF_8)
}