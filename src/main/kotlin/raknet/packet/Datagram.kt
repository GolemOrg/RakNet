package raknet.packet

import io.netty.buffer.ByteBuf
import raknet.Frame
import raknet.UIntLE
import raknet.enums.Flag

class Datagram(
    private val flags: Array<Flag>,
    private val sequenceIndex: UIntLE,
    private val frames: MutableList<Frame>
) {

    fun add(frame: Frame) {
        frames.add(frame)
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

    override fun toString(): String {
        return "Datagram(flags=Flags(${flags.joinToString(",")}), sequenceIndex=$sequenceIndex, frames=Frames(${frames.joinToString(",")}))"
    }
}