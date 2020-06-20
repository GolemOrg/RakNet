package net.golem.raknet.session.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.protocol.AcknowledgePacket;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketFactory;
import net.golem.raknet.protocol.RawPacket;
import net.golem.raknet.protocol.connected.ConnectedPingPacket;
import net.golem.raknet.protocol.connected.ConnectedPongPacket;
import net.golem.raknet.protocol.connected.DisconnectionNotificationPacket;
import net.golem.raknet.protocol.connected.connection.ConnectionRequestAcceptedPacket;
import net.golem.raknet.protocol.connected.connection.ConnectionRequestPacket;
import net.golem.raknet.protocol.datagram.EncapsulatedPacket;
import net.golem.raknet.protocol.datagram.RakNetDatagram;
import net.golem.raknet.protocol.datagram.SplitPacketInfo;
import net.golem.raknet.session.RakNetSession;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class PacketDecodeLayer implements CodecLayer {

	private Set<Integer> ackQueue = new TreeSet<>();

	private ConcurrentHashMap<Integer, EncapsulatedPacket[]> splitPackets = new ConcurrentHashMap<>();

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

		if(ackQueue.size() > 0) {
			AcknowledgePacket pk = AcknowledgePacket.createACK();
			pk.records = ackQueue;
			getSession().sendPacket(pk);
			ackQueue.clear();
		}
	}

	@Override
	public void close() {

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

	public void onConnectedPing(ConnectedPingPacket packet) {
		ConnectedPongPacket pk = new ConnectedPongPacket();
		pk.pingTime = packet.pingTime;
		pk.pongTime = session.getServer().getRakNetTimeMS();
	}

	public void handleDatagram(RakNetDatagram packet) {
		ackQueue.add(packet.sequenceNumber);
		if(packet.sequenceNumber > highestSequenceNumber) {
			highestSequenceNumber = packet.sequenceNumber;
		}
		for(EncapsulatedPacket pk : packet.packets) {
			handleEncapsulated(pk);
		}
	}

	public void handleEncapsulated(EncapsulatedPacket packet) {
		if(packet.splitInfo != null) {
			EncapsulatedPacket pk = handleSplit(packet);
			if(pk == null) {
				return;
			}
			packet = pk;
		}
		ByteBuf buffer = packet.buffer.copy();
		try {
			DataPacket pk = RakNetPacketFactory.from(buffer);
			if(!handle(pk)) {
				session.handle(pk);
				session.handleListeners(pk);
			}
		} finally {
			buffer.release();
		}
	}

	public EncapsulatedPacket handleSplit(EncapsulatedPacket packet) {
		SplitPacketInfo info = packet.splitInfo;
		if(info == null) {
			log.error("Encapsulated packet does not contain split info!");
			return null;
		}

		int index = info.splitIndex;
		int count = info.splitCount;

		EncapsulatedPacket[] split = splitPackets.getOrDefault(info.splitId, new EncapsulatedPacket[count]);
		split[index] = packet;

		splitPackets.put(info.splitId, split);

		for(EncapsulatedPacket part : split) {
			if(part == null) {
				// not finished assembling the packet yet
				return null;
			}
		}
		EncapsulatedPacket pk = new EncapsulatedPacket();
		pk.buffer = Unpooled.buffer();
		pk.reliability = packet.reliability;
		pk.messageIndex = packet.messageIndex;
		pk.sequenceIndex = packet.sequenceIndex;
		pk.orderIndex = packet.orderIndex;
		pk.orderChannel = packet.orderChannel;

		Arrays.stream(split).forEach(encapsulated -> pk.buffer.writeBytes(encapsulated.buffer));
		splitPackets.remove(index);
		return pk;
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
			return true;
		}
		if(packet instanceof ConnectedPingPacket) {
			this.onConnectedPing((ConnectedPingPacket) packet);
		}
		return false;
	}

}
