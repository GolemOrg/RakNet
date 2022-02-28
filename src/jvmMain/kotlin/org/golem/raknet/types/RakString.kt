package org.golem.raknet.types

import io.netty.buffer.ByteBuf
import org.golem.raknet.codec.Encodable

@JvmInline
value class RakString(val value: String): Encodable {
    override fun encode(buffer: ByteBuf) {
        buffer.writeShort(value.length)
        buffer.writeCharSequence(value, Charsets.UTF_8)
    }

    override fun toString(): String = value
}