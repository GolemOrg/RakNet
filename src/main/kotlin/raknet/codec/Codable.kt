package raknet.codec

import io.netty.buffer.ByteBuf

interface Codable {

    fun encode(buffer: ByteBuf): ByteBuf

    fun decode(buffer: ByteBuf): Any

}