package net.golem.raknet.protocol;

import io.netty.buffer.ByteBuf;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.enums.BitFlags;
import net.golem.raknet.protocol.connected.ConnectedPingPacket;
import net.golem.raknet.protocol.connected.DisconnectionNotificationPacket;
import net.golem.raknet.protocol.connected.NewIncomingConnectionPacket;
import net.golem.raknet.protocol.connected.connection.ConnectionRequestPacket;
import net.golem.raknet.protocol.connected.request.OpenConnectionRequest1Packet;
import net.golem.raknet.protocol.connected.request.OpenConnectionRequest2Packet;
import net.golem.raknet.protocol.datagram.RakNetDatagram;
import net.golem.raknet.protocol.unconnected.UnconnectedPingPacket;

public final class RakNetPacketFactory {

	public static DataPacket from(ByteBuf buffer) {
		PacketDecoder decoder = new PacketDecoder(buffer);
		int id = buffer.readUnsignedByte();
		DataPacket packet;
		if((id & BitFlags.VALID.getId()) != 0) {
			if((id & BitFlags.ACK.getId()) != 0) {
				packet = AcknowledgePacket.createACK();
			} else if((id & BitFlags.NAK.getId()) != 0){
				packet = AcknowledgePacket.createNAK();
			} else {
				packet = new RakNetDatagram();
			}
		} else {
			switch(id) {
				case RakNetPacketIds.UNCONNECTED_PING:
					packet = new UnconnectedPingPacket();
					break;
				case RakNetPacketIds.CONNECTED_PING:
					packet = new ConnectedPingPacket();
					break;
				case RakNetPacketIds.OPEN_CONNECTION_REQUEST_1:
					packet = new OpenConnectionRequest1Packet();
					break;
				case RakNetPacketIds.OPEN_CONNECTION_REQUEST_2:
					packet = new OpenConnectionRequest2Packet();
					break;
				case RakNetPacketIds.CONNECTION_REQUEST:
					packet = new ConnectionRequestPacket();
					break;
				case RakNetPacketIds.DISCONNECTION_REQUEST:
					packet = new DisconnectionNotificationPacket();
					break;
				case RakNetPacketIds.NEW_INCOMING_CONNECTION:
					packet = new NewIncomingConnectionPacket();
					break;
				default:
					packet = new RawPacket(id);
			}
		}
		packet.decode(decoder);
		return packet;
	}

}
