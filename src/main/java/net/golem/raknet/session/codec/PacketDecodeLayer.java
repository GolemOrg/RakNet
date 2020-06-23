package net.golem.raknet.session.codec;

import lombok.extern.log4j.Log4j2;
import net.golem.raknet.protocol.acknowledge.AcknowledgePacket;
import net.golem.raknet.protocol.connected.ConnectedPingPacket;
import net.golem.raknet.protocol.connected.ConnectedPongPacket;
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
import net.golem.raknet.session.EncapsulatedSplitHandler;
import net.golem.raknet.session.RakNetSession;
import net.golem.raknet.session.SessionListener;
import net.golem.raknet.session.SessionState;

import java.util.TreeSet;

@Log4j2
public class PacketDecodeLayer extends CodecLayer {

	private EncapsulatedSplitHandler splitHandler = new EncapsulatedSplitHandler(this);

	private long lastQueueSend = System.currentTimeMillis();
	private TreeSet<Integer> ackQueue = new TreeSet<>();

	public PacketDecodeLayer(RakNetSession session) {
		super(session);
	}

	@Override
	public void update(long currentTime) {
		checkQueues(currentTime);
	}

	public void checkQueues(long currentTime) {
		long difference = currentTime - lastQueueSend;
		if(ackQueue.size() > 0) {
			AcknowledgePacket packet = AcknowledgePacket.createACK();
			packet.records = ackQueue;
			session.sendPacket(packet);
			ackQueue.clear();
			lastQueueSend = System.currentTimeMillis();
		}
	}

	@Override
	public void close() {
		ackQueue.clear();
		splitHandler.close();
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
		datagram.packets.forEach(this::handleEncapsulated);
	}

	public void handleEncapsulated(EncapsulatedPacket packet) {
		EncapsulatedPacket encapsulatedPacket = packet;
		if(packet.splitInfo != null) {
			EncapsulatedPacket assembled = splitHandler.handle(packet);
			if(assembled == null) {
				return;
			}
			encapsulatedPacket = assembled;
		}
		DataPacket pk = EncapsulatedPacketFactory.from(new PacketDecoder(encapsulatedPacket.buffer));
		if(!handle(pk)) {
			session.handle(pk);
			session.getListeners().forEach(listener -> listener.onPacket(pk));
		}
	}

	public boolean handle(DataPacket packet) {
		if(packet instanceof ConnectionRequestPacket) {
			ConnectionRequestAcceptedPacket pk = new ConnectionRequestAcceptedPacket();
			pk.clientAddress = session.getAddress();
			pk.pingTime = ((ConnectionRequestPacket) packet).pingTime;
			pk.pongTime = session.getServer().getRakNetTimeMS();
			session.getEncodeLayer().sendEncapsulatedPacket(pk, PacketReliability.UNRELIABLE, 0);
			return true;
		}
		if(packet instanceof DisconnectionNotificationPacket) {
			session.close("client disconnect");
			return true;
		}
		if(packet instanceof NewIncomingConnectionPacket) {
			if(((NewIncomingConnectionPacket) packet).address.getPort() == session.getServer().getLocalAddress().getPort()) {
				session.setState(SessionState.CONNECTED);
				session.getServer().getListener().openSession(session.getAddress());
				session.getListeners().forEach(SessionListener::onOpen);
				session.ping();
			}
			return true;
		}
		if(packet instanceof ConnectedPingPacket) {
			handleConnectedPing((ConnectedPingPacket) packet);
			return true;
		}

		if(packet instanceof ConnectedPongPacket) {
			handleConnectedPong((ConnectedPongPacket) packet);
			return true;
		}
		return false;
	}

	public void handleConnectedPing(ConnectedPingPacket packet) {
		ConnectedPongPacket pk = new ConnectedPongPacket();
		pk.pingTime = packet.pingTime;
		pk.pongTime = session.getServer().getRakNetTimeMS();
		session.getEncodeLayer().sendEncapsulatedPacket(pk, PacketReliability.UNRELIABLE, 0);
	}

	public void handleConnectedPong(ConnectedPongPacket packet) {
		session.setLatency((int) (session.getServer().getRakNetTimeMS() - packet.pingTime));
	}
}
