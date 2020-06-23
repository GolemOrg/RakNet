package net.golem.raknet.session.codec;

import lombok.extern.log4j.Log4j2;
import net.golem.raknet.enums.PacketReliability;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.datagram.EncapsulatedPacket;
import net.golem.raknet.protocol.datagram.RakNetDatagram;
import net.golem.raknet.session.EncodeHandler;
import net.golem.raknet.session.RakNetSession;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
public class PacketEncodeLayer extends CodecLayer {

	/**
	 * IP header (20 bytes) + UDP header (8 bytes) + RakNet weird (8 bytes)
	 */
	public static final int LENGTH_OVERHEAD = 36;
	/**
	 * LENGTH_OVERHEAD + datagram header (4 bytes) + maximum encapsulated packet header (20 bytes)
	 */
	public static final int ENCAPSULATED_OVERHEAD = LENGTH_OVERHEAD + 24;

	private int currentSequenceNumber = 0;

	public ConcurrentLinkedQueue<EncapsulatedPacket> packetQueue = new ConcurrentLinkedQueue<>();

	private EncodeHandler encodeHandler = new EncodeHandler(this);

	public PacketEncodeLayer(RakNetSession session) {
		super(session);
	}

	@Override
	public void update(long currentTime) {
		sendQueue();
	}

	@Override
	public void close() {
		packetQueue.clear();
		encodeHandler.requiresAck.clear();
	}

	public void sendEncapsulatedPacket(DataPacket packet, PacketReliability reliability, @Nonnegative int orderChannel, boolean immediate) {
		EncapsulatedPacket pk = new EncapsulatedPacket();
		pk.buffer = packet.create();
		pk.reliability = reliability;
		pk.orderChannel = orderChannel;
		encodeHandler.passEncapsulated(pk, immediate);
	}

	public void sendEncapsulatedPacket(DataPacket packet, PacketReliability reliability, @Nonnegative int orderChannel) {
		sendEncapsulatedPacket(packet, reliability, orderChannel, true);
	}

	public void sendDatagram(RakNetDatagram datagram) {
		session.sendPacket(datagram);
	}

	public void sendQueue() {
		if(packetQueue.size() > 0) {
			RakNetDatagram datagram = new RakNetDatagram();
			datagram.packets = new ArrayList<>(packetQueue);
			datagram.sequenceIndex = currentSequenceNumber++;
			sendDatagram(datagram);
			packetQueue.clear();
		}
	}
}
