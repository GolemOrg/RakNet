package org.golem.raknet.message.datagram

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import org.golem.raknet.readToByteArray

class FrameBuilder(private val count: Int) {

    private val pendingBuffers: MutableMap<Int, ByteArray> = mutableMapOf()

    fun add(frame: Frame) {
        pendingBuffers[frame.fragment!!.index] = frame.body.readToByteArray(frame.body.readableBytes())
    }

    fun complete(): Boolean = pendingBuffers.size == count

    fun build(): ByteBuf {
        if (!complete()) throw IllegalStateException("FrameBuilder is not complete")
        val buffer = ByteBufAllocator.DEFAULT.ioBuffer()
        pendingBuffers.forEach { buffer.writeBytes(it.value) }
        pendingBuffers.clear()
        return buffer
    }

}