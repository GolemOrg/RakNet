package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import raknet.Frame
import raknet.UIntLE
import raknet.enums.Flag
import kotlin.experimental.or

/**
 * The packet ID passed to the parent constructor isn't the actual packet ID, but rather a simple placeholder
 * so that we can send it like any other packet.
 */
class Datagram(
    val flags: Array<Flag> = arrayOf(Flag.DATAGRAM),
    val sequenceIndex: UIntLE,
    val frames: MutableList<Frame>
) : DataPacket(Flag.DATAGRAM.id()) {

    override fun encodeHeader(buffer: ByteBuf): ByteBuf =
        buffer.writeByte(flags.fold(0b0000_0000.toByte()) { accumulatedFlags, flag ->
            accumulatedFlags or flag.id().toByte()
        }.toInt())

    override fun encode(): ByteBuf {
        val buffer = ByteBufAllocator.DEFAULT.ioBuffer()
        encodeHeader(buffer)
        buffer.writeMediumLE(sequenceIndex.toInt())
        frames.forEach { buffer.writeBytes(it.encode()) }
        return buffer
    }

    companion object {
        fun from(bitFlags: Short, buffer: ByteBuf): Datagram {
            val flags = Flag.from(bitFlags)
            val sequenceIndex = buffer.readUnsignedMediumLE().toUInt()
            val frames = mutableListOf<Frame>()
            while (buffer.isReadable) {
                frames.add(Frame.from(buffer))
            }
            return Datagram(flags, sequenceIndex, frames)
        }
    }

    override fun encodeOrder(): Array<Any> = arrayOf() // Since we override encode(), this isn't needed

    override fun toString() =
        "Datagram(flags=Flags(${flags.joinToString(",")}), sequenceIndex=$sequenceIndex, frames=Frames(${
            frames.joinToString(",")
        }))"
}