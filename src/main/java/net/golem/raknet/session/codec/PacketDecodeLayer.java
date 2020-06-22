package net.golem.raknet.session.codec;

import net.golem.raknet.protocol.acknowledge.AcknowledgePacket;
import net.golem.raknet.protocol.datagram.EncapsulatedPacketFactory;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.enums.PacketReliability;
import net.golem.raknet.protocol.*;
import net.golem.raknet.protocol.connected.DisconnectionNotificationPacket;
import net.golem.raknet.protocol.connected.NewIncomingConnectionPacket;
import net.golem.raknet.protocol.connected.connection.ConnectionRequestAcceptedPacket;
import net.golem.raknet.protocol.connected.connection.ConnectionRequestPacket;
import net.golem.raknet.protocol.datagram.EncapsulatedPacket;
import net.golem.raknet.protocol.datagram.RakNetDatagram;
import net.golem.raknet.session.RakNetSession;
import net.golem.raknet.session.SessionState;

import java.util.TreeSet;

public class PacketDecodeLayer extends CodecLayer {

	private TreeSet<Integer> ackQueue = new TreeSet<>();

	public PacketDecodeLayer(RakNetSession session) {
		super(session);
	}

	@Override
	public void update(long currentTime) {

		checkQueues();
	}

	public void checkQueues() {
		if(ackQueue.size() > 0) {
			AcknowledgePacket packet = AcknowledgePacket.createACK();
			packet.records = ackQueue;
			session.sendPacket(packet);
			ackQueue.clear();
		}
	}

	@Override
	public void close() {

	}

	public void handleIncoming(DataPacket packet) {
		if(packet instanceof RakNetDatagram) {
			handleDatagram((RakNetDatagram) packet);
		} else if(packet instanceof AcknowledgePacket) {
			if(((AcknowledgePacket) packet).isACK()) {
				handleACK((AcknowledgePacket) packet);
			} else if(((AcknowledgePacket) packet).isNAK()) {
				handleNAK((AcknowledgePacket) packet);
			}
		} else {
			handle(packet);
		}
	}

	public void handleACK(AcknowledgePacket packet) {

	}

	public void handleNAK(AcknowledgePacket packet) {

	}

	public void handleDatagram(RakNetDatagram datagram) {
		ackQueue.add(datagram.sequenceIndex);
		for(EncapsulatedPacket packet : datagram.packets) handleEncapsulated(packet);
	}

	public void handleEncapsulated(EncapsulatedPacket packet) {
		DataPacket pk = EncapsulatedPacketFactory.from(new PacketDecoder(packet.buffer));
		handle(pk);
	}

	public void handle(DataPacket packet) {
		if(packet instanceof ConnectionRequestPacket) {
			ConnectionRequestAcceptedPacket pk = new ConnectionRequestAcceptedPacket();
			pk.clientAddress = session.getAddress();
			pk.pingTime = ((ConnectionRequestPacket) packet).pingTime;
			pk.pongTime = session.getServer().getRakNetTimeMS();
			session.getEncodeLayer().sendEncapsulatedPacket(pk, PacketReliability.UNRELIABLE, 0);
			return;
		}
		if(packet instanceof DisconnectionNotificationPacket) {
			session.close("client disconnect");
			return;
		}
		if(packet instanceof NewIncomingConnectionPacket) {
			if(((NewIncomingConnectionPacket) packet).address.getPort() == session.getServer().getLocalAddress().getPort()) {
				session.setState(SessionState.CONNECTED);
				session.ping();
			}
		}
	}
}
