package raknet.codec

import io.netty.buffer.ByteBuf
import raknet.encode

interface OrderedEncodable: Encodable {
    fun encodeOrder(): Array<Any>

    override fun encode(buffer: ByteBuf) { encodeOrder().forEach { it.encode(buffer) } }
}