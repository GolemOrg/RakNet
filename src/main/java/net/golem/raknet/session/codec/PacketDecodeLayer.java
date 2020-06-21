package net.golem.raknet.session.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.protocol.*;
import net.golem.raknet.protocol.connected.ConnectedPingPacket;
import net.golem.raknet.protocol.connected.ConnectedPongPacket;
import net.golem.raknet.protocol.connected.DisconnectionNotificationPacket;
import net.golem.raknet.protocol.connected.NewIncomingConnectionPacket;
import net.golem.raknet.protocol.connected.connection.ConnectionRequestAcceptedPacket;
import net.golem.raknet.protocol.connected.connection.ConnectionRequestPacket;
import net.golem.raknet.protocol.datagram.EncapsulatedPacket;
import net.golem.raknet.protocol.datagram.RakNetDatagram;
import net.golem.raknet.protocol.datagram.SplitPacketInfo;
import net.golem.raknet.session.RakNetSession;
import net.golem.raknet.session.SessionState;

import java.util.Set;
import java.util.TreeSet;

@Log4j2
public class PacketDecodeLayer implements CodecLayer {

	private Set<Integer> ackQueue = new TreeSet<>();
	private Set<Integer> nakQueue = new TreeSet<>();

	private int highestSequenceNumber = 0;

	private RakNetSession session;

	public PacketDecodeLayer(RakNetSession session) {
		this.session = session;
	}

	public RakNetSession getSession() {
		return session;
	}

	@Override
	public void update(long currentTime) {

		checkQueues();
	}

	@Override
	public void close() {

	}

	public void checkQueues() {
		if(ackQueue.size() > 0) {
			AcknowledgePacket pk = AcknowledgePacket.createACK();
			pk.records = ackQueue;
			session.sendPacket(pk);
			ackQueue.clear();
		}

		if(nakQueue.size() > 0) {
			AcknowledgePacket pk = AcknowledgePacket.createNAK();
			pk.records = nakQueue;
			session.sendPacket(pk);
			nakQueue.clear();
		}
	}

	public void handleIncoming(DataPacket packet) {
		if(packet instanceof AcknowledgePacket) {
			if(((AcknowledgePacket) packet).isACK()) {
				handleACK((AcknowledgePacket) packet);
			} else if(((AcknowledgePacket) packet).isNAK()) {
				handleNAK((AcknowledgePacket) packet);
			} else {
				log.error("Unknown AcknowledgePacket type: {}", packet.getPacketId());
			}
		} else if(packet instanceof RakNetDatagram) {
			handleDatagram((RakNetDatagram) packet);
		} else if(packet instanceof RawPacket) {
			handleRaw((RawPacket) packet);
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
		if(datagram.sequenceIndex > highestSequenceNumber) highestSequenceNumber = datagram.sequenceIndex;
		for(EncapsulatedPacket packet : datagram.packets) {
			handleEncapsulated(packet);
		}
	}

	public void handleEncapsulated(EncapsulatedPacket packet) {
		DataPacket pk = RakNetPacketFactory.from(packet.buffer);
		if(pk instanceof RawPacket) {
			log.error("Unknown data packet: {}", pk);
			return;
		}
		handle(pk);
	}

	public void onConnectedPing(ConnectedPingPacket packet) {
		ConnectedPongPacket pk = new ConnectedPongPacket();
		pk.pingTime = packet.pingTime;
		pk.pongTime = session.getServer().getRakNetTimeMS();
		session.getEncodeLayer().sendEncapsulatedPacket(pk);
	}

	public void handleRaw(RawPacket packet) {
		if(session.getServer().verbose) log.info("Raw packet found: ");
		if(session.getServer().verbose) log.info("Packet ID: {}", packet.getPacketId());
	}

	public boolean handle(DataPacket packet) {
		session.updateReceivedTime();
		if(packet instanceof DisconnectionNotificationPacket) {
			session.close("client disconnect");
			return true;
		}
		if(packet instanceof ConnectionRequestPacket) {
			ConnectionRequestAcceptedPacket pk = new ConnectionRequestAcceptedPacket();
			pk.clientAddress = session.getAddress();
			pk.pingTime = ((ConnectionRequestPacket) packet).pingTime;
			pk.pongTime = session.getServer().getRakNetTimeMS();

			session.getEncodeLayer().sendEncapsulatedPacket(pk);
			session.setState(SessionState.CONNECTING);
			return true;
		}
		if(packet instanceof ConnectedPingPacket) {
			onConnectedPing((ConnectedPingPacket) packet);
			return true;
		}

		if(packet instanceof NewIncomingConnectionPacket) {
			// do something here
			return true;
		}
		return false;
	}

}
