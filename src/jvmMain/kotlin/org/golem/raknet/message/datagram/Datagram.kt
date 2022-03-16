package org.golem.raknet.message.datagram

import io.netty.buffer.ByteBuf
import io.netty.util.ReferenceCounted
import org.golem.raknet.enums.Flags
import org.golem.raknet.message.OnlineMessage
import org.golem.netty.types.UMediumLE

class Datagram(
    val flags: MutableList<Flags>,
    val datagramSequenceNumber: UMediumLE,
    val frames: MutableList<Frame>,
): OnlineMessage(Flags.DATAGRAM.id()), ReferenceCounted {

    override fun encodeHeader(buffer: ByteBuf) {
        buffer.writeByte(flags.fold(0) { acc, flag -> acc or flag.id() })
    }

    override fun encodeOrder(): Array<Any> = arrayOf(
        datagramSequenceNumber,
        frames
    )

    override fun toString(): String = "Datagram(flags=$flags, datagramSequenceNumber=$datagramSequenceNumber, frames=$frames)"

    override fun refCnt(): Int = 0

    override fun retain() = retain(1)

    override fun retain(increment: Int): ReferenceCounted {
        frames.forEach { it.body.retain(increment) }
        return this
    }

    override fun touch() = this

    override fun touch(hint: Any?) = touch()

    override fun release() = release(1)

    override fun release(decrement: Int): Boolean {
        frames.forEach { it.body.release(decrement) }
        return true
    }

    companion object {
        fun from(buffer: ByteBuf): Datagram {
            val flags = Flags.from(buffer.readUnsignedByte().toInt()).toMutableList()
            val datagramSequenceNumber = UMediumLE(buffer.readUnsignedMediumLE().toUInt())
            val frames = mutableListOf<Frame>()
            while (buffer.isReadable) {
                val frame = Frame.from(buffer)
                frames.add(frame)
            }
            return Datagram(flags, datagramSequenceNumber, frames)
        }
    }
}