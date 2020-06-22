package net.golem.raknet.handler;

import io.netty.channel.DefaultAddressedEnvelope;
import net.golem.raknet.protocol.DataPacket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class RakNetPacketEnvelope<I extends DataPacket, I1 extends SocketAddress> extends DefaultAddressedEnvelope<I, InetSocketAddress> {

	public RakNetPacketEnvelope(I packet, InetSocketAddress recipient) {
		super(packet, recipient);
	}

}
