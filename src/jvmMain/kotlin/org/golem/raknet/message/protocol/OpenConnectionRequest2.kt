package org.golem.raknet.message.protocol

import io.netty.buffer.ByteBuf
import org.golem.netty.readAddress
import org.golem.raknet.types.Magic
import org.golem.raknet.types.Magic.readMagic
import org.golem.raknet.message.OfflineMessage
import org.golem.raknet.message.MessageType
import java.net.InetSocketAddress

class OpenConnectionRequest2(
    var magic: Magic,
    var serverAddress: InetSocketAddress,
    var mtuSize: Short,
    var clientGuid: Long,
): OfflineMessage(MessageType.OPEN_CONNECTION_REQUEST_2.id()) {

    override fun encodeOrder(): Array<Any> = arrayOf(magic, serverAddress, mtuSize, clientGuid)

    companion object {
        fun from(buffer: ByteBuf) = OpenConnectionRequest2(
            buffer.readMagic(),
            buffer.readAddress(),
            buffer.readUnsignedShort().toShort(),
            buffer.readLong()
        )
    }

    override fun toString() = "OpenConnectionRequest2(magic=$magic, serverAddress=$serverAddress, mtuSize=$mtuSize, clientGuid=$clientGuid)"

}