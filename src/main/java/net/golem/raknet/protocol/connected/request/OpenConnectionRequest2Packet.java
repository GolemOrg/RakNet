package net.golem.raknet.protocol.connected.request;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

import java.net.InetSocketAddress;

public class OpenConnectionRequest2Packet extends DataPacket {

	public long clientId;

	public InetSocketAddress serverAddress;

	public short maximumTransferUnits;

	public OpenConnectionRequest2Packet() {
		super(RakNetPacketIds.OPEN_CONNECTION_REQUEST_2);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeMagic();
		encoder.writeAddress(serverAddress);
		encoder.writeShort(maximumTransferUnits);
		encoder.writeLong(clientId);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		decoder.skipMagic();
		serverAddress = decoder.readAddress();
		maximumTransferUnits = decoder.readShort();
		clientId = decoder.readLong();
	}

	@Override
	public String toString() {
		return "OpenConnectionRequest2Packet{" +
				"clientId=" + clientId +
				", serverAddress=" + serverAddress +
				", maximumTransferUnits=" + maximumTransferUnits +
				'}';
	}
}
