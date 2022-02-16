package org.golem.raknet.codec

import io.netty.buffer.ByteBuf

interface Decodable {
    fun decode(buffer: ByteBuf): Any?
}