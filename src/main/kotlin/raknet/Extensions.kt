package raknet

import io.netty.buffer.ByteBuf


fun ByteBuf.readToByteArray(length: Int): ByteArray {
    val bytes = ByteArray(length)
    readBytes(bytes)
    return bytes
}