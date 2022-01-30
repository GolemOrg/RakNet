package raknet

import io.netty.buffer.ByteBuf
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.experimental.*

fun ByteBuf.readAddress(): InetSocketAddress {
    val addressBytes: ByteArray
    val port: Int
    when(val type: Int = readByte().toInt()) {
        4 -> {
            addressBytes = readToByteArray(4).map { it.inv().and(0xFF.toByte()) }.toByteArray()
            port = readUnsignedShort()
        }
        6 -> {
            readShort() // AF_INET6
            port = readUnsignedShort()
            readInt() // Flow Info
            addressBytes = readToByteArray(16)
            readInt() // Scope ID
        }
        else -> throw IllegalArgumentException("Unknown address type $type")
    }
    return InetSocketAddress(InetAddress.getByAddress(addressBytes), port)
}

fun ByteBuf.readToByteArray(length: Int): ByteArray {
    val bytes = ByteArray(length)
    readBytes(bytes)
    return bytes
}