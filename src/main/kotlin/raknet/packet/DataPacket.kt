package raknet.packet

import raknet.codec.DecodeResult

abstract class DataPacket(private val id: Byte) : Packet {

    override fun encode(): ByteArray {
        // Return an empty payload for now
        return ByteArray(0)
    }

    abstract override fun decode(buffer: ByteArray): DecodeResult

    fun prepare(): ByteArray {
        return byteArrayOf(id).plus(encode())
    }

    override fun toString(): String {
        return "DataPacket(id=$id)"
    }

}