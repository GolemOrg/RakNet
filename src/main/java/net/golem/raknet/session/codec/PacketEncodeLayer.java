package net.golem.raknet.session.codec;

import lombok.extern.log4j.Log4j2;
import net.golem.raknet.enums.PacketReliability;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.datagram.EncapsulatedPacket;
import net.golem.raknet.protocol.datagram.RakNetDatagram;
import net.golem.raknet.session.RakNetSession;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
public class PacketEncodeLayer implements CodecLayer {

	protected ConcurrentLinkedQueue<EncapsulatedPacket> packetQueue = new ConcurrentLinkedQueue<>();

	private int sequenceNumber = 0;

	private TreeMap<Integer, ArrayList<Integer>> awaitingAcks = new TreeMap<>();

	private RakNetSession session;

	public PacketEncodeLayer(RakNetSession session) {
		this.session = session;
	}

	public RakNetSession getSession() {
		return session;
	}

	public void sendEncapsulatedPacket(DataPacket packet, PacketReliability reliability, @Nonnegative int orderChannel, boolean immediate) {
		EncapsulatedPacket pk = new EncapsulatedPacket();
		pk.reliability = reliability;
		pk.orderChannel = orderChannel;
		pk.buffer = packet.create();
		if(immediate) {
			RakNetDatagram datagram = new RakNetDatagram();
			datagram.packets.add(pk);
			datagram.sequenceIndex = sequenceNumber++;
			sendDatagram(datagram);
		} else {
			packetQueue.add(pk);
		}
	}

	public void sendEncapsulatedPacket(DataPacket packet, PacketReliability reliability, int orderChannel) {
		sendEncapsulatedPacket(packet, reliability, orderChannel, true);
	}

	public void sendEncapsulatedPacket(DataPacket packet, PacketReliability reliability) {
		sendEncapsulatedPacket(packet, reliability, 0);
	}

	public void sendEncapsulatedPacket(DataPacket packet) {
		sendEncapsulatedPacket(packet, PacketReliability.UNRELIABLE);
	}


	public void sendDatagram(RakNetDatagram datagram) {
		session.sendPacket(datagram);
	}


	@Override
	public void update(long currentTime) {
		sendQueue();
	}

	@Override
	public void close() {
		packetQueue.clear();
	}

	public void sendQueue() {
		if(packetQueue.size() > 0) {
			RakNetDatagram datagram = new RakNetDatagram();
			datagram.packets = new ArrayList<>(datagram.packets);

			packetQueue.clear();
		}
	}
}
