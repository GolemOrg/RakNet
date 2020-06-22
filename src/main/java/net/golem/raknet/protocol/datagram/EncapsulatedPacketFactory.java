package net.golem.raknet.protocol.datagram;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;
import net.golem.raknet.protocol.RawPacket;
import net.golem.raknet.protocol.connected.ConnectedPingPacket;
import net.golem.raknet.protocol.connected.DisconnectionNotificationPacket;
import net.golem.raknet.protocol.connected.NewIncomingConnectionPacket;
import net.golem.raknet.protocol.connected.connection.ConnectionRequestPacket;

public final class EncapsulatedPacketFactory {

	public static DataPacket from(PacketDecoder decoder) {
		int packetId = decoder.readUnsignedByte();
		DataPacket packet;
		switch(packetId) {
			case RakNetPacketIds.CONNECTION_REQUEST:
				packet = new ConnectionRequestPacket();
				break;
			case RakNetPacketIds.DISCONNECTION_REQUEST:
				packet = new DisconnectionNotificationPacket();
				break;
			case RakNetPacketIds.NEW_INCOMING_CONNECTION:
				packet = new NewIncomingConnectionPacket();
				break;
			case RakNetPacketIds.CONNECTED_PING:
				packet = new ConnectedPingPacket();
				break;
			default:
				packet = new RawPacket(packetId);
		}
		packet.decode(decoder);
		return packet;
	}
}
