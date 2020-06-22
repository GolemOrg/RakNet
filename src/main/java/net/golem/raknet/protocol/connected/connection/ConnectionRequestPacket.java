package net.golem.raknet.protocol.connected.connection;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

public class ConnectionRequestPacket extends DataPacket {

	public long clientId;

	public long pingTime;

	public boolean useSecurity = false;

	public ConnectionRequestPacket() {
		super(RakNetPacketIds.CONNECTION_REQUEST);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeLong(clientId);
		encoder.writeLong(pingTime);
		encoder.writeBoolean(useSecurity);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		clientId = decoder.readLong();
		pingTime = decoder.readLong();
		useSecurity = decoder.readBoolean();
	}

	@Override
	public String toString() {
		return "ConnectionRequestPacket{" +
				"clientId=" + clientId +
				", pingTime=" + pingTime +
				", useSecurity=" + useSecurity +
				'}';
	}
}
