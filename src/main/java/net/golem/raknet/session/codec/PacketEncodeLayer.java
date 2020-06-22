package net.golem.raknet.session.codec;

import net.golem.raknet.enums.PacketReliability;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.datagram.EncapsulatedPacket;
import net.golem.raknet.protocol.datagram.RakNetDatagram;
import net.golem.raknet.session.RakNetSession;

import javax.annotation.Nonnegative;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PacketEncodeLayer extends CodecLayer {

	public ConcurrentLinkedQueue<EncapsulatedPacket> packetQueue = new ConcurrentLinkedQueue<>();

	private int currentSequenceNumber = 0;

	public PacketEncodeLayer(RakNetSession session) {
		super(session);
	}

	@Override
	public void update(long currentTime) {

	}

	@Override
	public void close() {

	}

	public void sendEncapsulatedPacket(DataPacket packet, PacketReliability reliability, @Nonnegative int orderChannel, boolean immediate) {
		EncapsulatedPacket pk = new EncapsulatedPacket();
		pk.buffer = packet.create();
		pk.reliability = reliability;
		pk.orderChannel = orderChannel;

		if(immediate) {
			RakNetDatagram datagram = new RakNetDatagram();
			datagram.packets.add(pk);
			datagram.sequenceIndex = currentSequenceNumber++;
			sendDatagram(datagram);
		} else {
			packetQueue.add(pk);
		}
	}

	public void sendEncapsulatedPacket(DataPacket packet, PacketReliability reliability, @Nonnegative int orderChannel) {
		sendEncapsulatedPacket(packet, reliability, orderChannel, true);
	}

	public void sendDatagram(RakNetDatagram datagram) {
		session.sendPacket(datagram);
	}
}
