package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import raknet.UIntLE
import raknet.enums.Flag

/**
 * The packet ID passed to the parent constructor isn't the actual packet ID, but rather a simple placeholder
 * so that we can send it like any other packet.
 */
class Datagram(
    val header: Array<Flag> = arrayOf(Flag.DATAGRAM),
    val sequenceIndex: UIntLE,
    val frames: MutableList<Frame>
) : ConnectedPacket(Flag.DATAGRAM.id()) {

    override fun encodeHeader(buffer: ByteBuf): ByteBuf = buffer.writeByte(header.fold(0) { acc, flag -> acc or flag.id() })

    override fun encode(): ByteBuf {
        val buffer = ByteBufAllocator.DEFAULT.ioBuffer()
        buffer.writeMediumLE(sequenceIndex.toInt())
        frames.forEach {
            val encoded = it.encode()
            try {
                buffer.writeBytes(encoded)
            } finally {
                encoded.release()
            }
        }
        return buffer
    }

    companion object {
        fun from(buffer: ByteBuf): Datagram {
            val flags = Flag.from(buffer.readUnsignedByte().toInt())
            val sequenceIndex = buffer.readUnsignedMediumLE().toUInt()
            val frames = mutableListOf<Frame>()
            while (buffer.isReadable) {
                val frame = Frame.from(buffer)
                frames.add(frame)
            }
            return Datagram(flags, sequenceIndex, frames)
        }
    }

    override fun encodeOrder(): Array<Any> = arrayOf() // Since we override encode(), this isn't needed

    override fun toString() =
        "Datagram(flags=Flags(${header.joinToString(",")}), sequenceIndex=$sequenceIndex, frames=Frames(${
            frames.joinToString(",")
        }))"
}