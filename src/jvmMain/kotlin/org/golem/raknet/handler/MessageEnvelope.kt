package org.golem.raknet.handler

import io.netty.channel.DefaultAddressedEnvelope
import org.golem.raknet.message.DataMessage
import java.net.InetSocketAddress

class MessageEnvelope<T: DataMessage>(
    packet: T,
    address: InetSocketAddress
): DefaultAddressedEnvelope<T, InetSocketAddress>(packet, null, address) {

    /**
     * Given that the error usually returned by `super.release()` is a IllegalReferenceCountException,
     * we can safely ignore it and default to true.
     */
    override fun release(): Boolean = runCatching { super.release() }.getOrDefault(true)
}