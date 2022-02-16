package org.golem.raknet.codec

import io.netty.buffer.ByteBuf
import org.golem.raknet.encode

interface OrderedEncodable: Encodable {
    fun encodeOrder(): Array<Any>

    override fun encode(buffer: ByteBuf) { encodeOrder().encode(buffer) }
}