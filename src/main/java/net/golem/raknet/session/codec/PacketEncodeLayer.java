package net.golem.raknet.session.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.enums.PacketPriority;
import net.golem.raknet.enums.PacketReliability;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.datagram.EncapsulatedPacket;
import net.golem.raknet.protocol.datagram.RakNetDatagram;
import net.golem.raknet.session.RakNetSession;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
public class PacketEncodeLayer implements CodecLayer {

	protected ConcurrentLinkedQueue<EncapsulatedPacket> packetQueue = new ConcurrentLinkedQueue<>();

	private int sequenceNumber = 0;

	private RakNetSession session;

	public PacketEncodeLayer(RakNetSession session) {
		this.session = session;
	}

	public RakNetSession getSession() {
		return session;
	}

	public void sendEncapsulatedPacket(DataPacket packet, PacketReliability reliability, PacketPriority priority, @Nonnegative int orderChannel) {
		if(session.isClosed()) {
			return;
		}
		EncapsulatedPacket encapsulated = new EncapsulatedPacket();
		encapsulated.reliability = reliability;
		encapsulated.orderChannel = orderChannel;
		encapsulated.buffer = packet.create();
		if(priority != PacketPriority.IMMEDIATE) {
			packetQueue.add(encapsulated);
		} else {
			RakNetDatagram datagram = new RakNetDatagram();
			datagram.packets.add(encapsulated);
			sendDatagram(datagram);
		}
	}

	public void sendEncapsulatedPacket(DataPacket packet, PacketReliability reliability, PacketPriority priority) {
		sendEncapsulatedPacket(packet, reliability, priority, 0);
	}

	public void sendEncapsulatedPacket(DataPacket packet, PacketReliability reliability) {
		sendEncapsulatedPacket(packet, reliability, PacketPriority.IMMEDIATE);
	}

	public void sendEncapsulatedPacket(DataPacket packet) {
		sendEncapsulatedPacket(packet, PacketReliability.UNRELIABLE);
	}

	public void sendDatagram(RakNetDatagram datagram) {
		datagram.sequenceNumber = sequenceNumber++;
		session.getContext().writeAndFlush(new DatagramPacket(datagram.create(), getSession().getAddress()));
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
			sendDatagram(datagram);
			packetQueue.clear();
		}
	}
}
