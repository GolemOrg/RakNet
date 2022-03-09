package org.golem.raknet.message.datagram

import org.golem.netty.codec.OrderedEncodable
import org.golem.netty.types.UMediumLE

data class Order(val index: UMediumLE, val channel: Byte): OrderedEncodable {
    override fun encodeOrder(): Array<Any> = arrayOf(index, channel)

    override fun toString(): String = "Order(index=$index, channel=$channel)"
}

data class Fragment(val count: Int, val fragmentId: Short, val index: Int) {
    override fun toString(): String = "Fragment(count=$count, fragmentId=$fragmentId, index=$index)"
}