package org.golem.raknet.message

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import org.golem.raknet.codec.OrderedEncodable

abstract class DataMessage(open val id: Int) : Message, OrderedEncodable {

    open fun encodeHeader(buffer: ByteBuf) {
        buffer.writeByte(id)
    }

    /**
     * We don't use decode at the moment as we use XPacket.from() as a way to decode the packet
     * It may be worth a look at using decode in the future
     */
    override fun decode(buffer: ByteBuf) = Unit

    fun prepare(): ByteBuf {
        val buffer = ByteBufAllocator.DEFAULT.ioBuffer()
        encodeHeader(buffer)
        encode(buffer)
        return buffer
    }

    override fun toString(): String = "DataPacket(id=$id)"

}

abstract class OnlineMessage(override val id: Int) : DataMessage(id)
abstract class OfflineMessage(override val id: Int): DataMessage(id)