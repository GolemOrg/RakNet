package raknet.handler

import io.netty.channel.DefaultAddressedEnvelope
import raknet.message.DataMessage
import java.net.InetSocketAddress

class MessageEnvelope<T: DataMessage>(
    packet: T,
    address: InetSocketAddress
): DefaultAddressedEnvelope<T, InetSocketAddress>(packet, null, address)