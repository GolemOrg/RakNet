package org.golem.raknet.message.datagram

import org.golem.raknet.codec.OrderedEncodable
import org.golem.raknet.types.UInt24LE

data class Order(val index: UInt24LE, val channel: Byte): OrderedEncodable {
    override fun encodeOrder(): Array<Any> = arrayOf(index, channel)

    override fun toString(): String = "Order(index=$index, channel=$channel)"
}

data class Fragment(val count: Int, val fragmentId: Short, val index: Int) {
    override fun toString(): String = "Fragment(count=$count, fragmentId=$fragmentId, index=$index)"
}