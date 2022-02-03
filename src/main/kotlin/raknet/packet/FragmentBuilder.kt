package raknet.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import raknet.packet.protocol.UnknownPacket
import raknet.readToByteArray

class FragmentBuilder(private val count: Int) {
    private val fragmentData: MutableMap<Int, ByteArray> = mutableMapOf()

    fun add(fragment: Fragment, buffer: ByteBuf) {
        fragmentData.putIfAbsent(fragment.index, buffer.readToByteArray(buffer.readableBytes()))
    }

    fun complete(): Boolean = fragmentData.size == count

    fun build(): UnknownPacket {
        if (!complete()) throw IllegalStateException("FragmentBuilder is not complete")
        val buffer = fragmentData.values.fold(ByteBufAllocator.DEFAULT.buffer()) { acc, bytes -> acc.writeBytes(bytes) }
        return UnknownPacket(
            id = buffer.readUnsignedByte().toInt(),
            buffer = buffer.retain()
        )
    }
}