package raknet.handler

import io.netty.channel.DefaultAddressedEnvelope
import raknet.packet.DataPacket
import java.net.InetSocketAddress

class PacketEnvelope<T: DataPacket>(
    packet: T,
    address: InetSocketAddress
): DefaultAddressedEnvelope<T, InetSocketAddress>(packet, null, address)