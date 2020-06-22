package net.golem.raknet.protocol;

import io.netty.buffer.ByteBuf;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.enums.BitFlags;
import net.golem.raknet.protocol.acknowledge.AcknowledgePacket;
import net.golem.raknet.protocol.connected.ConnectedPingPacket;
import net.golem.raknet.protocol.connected.request.OpenConnectionRequest1Packet;
import net.golem.raknet.protocol.connected.request.OpenConnectionRequest2Packet;
import net.golem.raknet.protocol.datagram.RakNetDatagram;
import net.golem.raknet.protocol.unconnected.UnconnectedPingPacket;

@Log4j2
public final class RakNetPacketFactory {

	public static DataPacket from(ByteBuf buffer) {
		PacketDecoder decoder = new PacketDecoder(buffer);
		short id = decoder.readUnsignedByte();
		DataPacket packet;
		if((id & BitFlags.VALID.getId()) != 0) {
			if((id & BitFlags.ACK.getId()) != 0) {
				packet = AcknowledgePacket.createACK();
			} else if((id & BitFlags.NAK.getId()) != 0) {
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
				default:
					packet = new RawPacket(id);
			}
		}
		try {
			packet.decode(decoder);
		} catch(Exception exception) {
			log.error("Packet: {}", packet.getClass().getSimpleName());
			log.error("Exception occurred: {}", exception.getMessage());
		}
		return packet;
	}

}
