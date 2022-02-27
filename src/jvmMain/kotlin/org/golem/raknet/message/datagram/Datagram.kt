package org.golem.raknet.message.datagram

import io.netty.buffer.ByteBuf
import org.golem.raknet.enums.Flags
import org.golem.raknet.message.OnlineMessage
import org.golem.raknet.types.UInt24LE

class Datagram(
    val flags: MutableList<Flags>,
    val datagramSequenceNumber: UInt24LE,
    val frames: MutableList<Frame>,
): OnlineMessage(Flags.DATAGRAM.id()){

    override fun encodeHeader(buffer: ByteBuf) {
        buffer.writeByte(flags.fold(0) { acc, flag -> acc or flag.id() })
    }

    override fun encodeOrder(): Array<Any> = arrayOf(
        datagramSequenceNumber,
        frames
    )

    override fun toString(): String = "Datagram(flags=$flags, datagramSequenceNumber=$datagramSequenceNumber, frames=$frames)"

    companion object {
        fun from(buffer: ByteBuf): Datagram {
            val flags = Flags.from(buffer.readUnsignedByte().toInt()).toMutableList()
            val datagramSequenceNumber = UInt24LE(buffer.readUnsignedMediumLE().toUInt())
            val frames = mutableListOf<Frame>()
            while (buffer.isReadable) {
                val frame = Frame.from(buffer)
                frames.add(frame)
            }
            return Datagram(flags, datagramSequenceNumber, frames)
        }
    }
}