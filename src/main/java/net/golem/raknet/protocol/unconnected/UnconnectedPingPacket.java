package net.golem.raknet.protocol.unconnected;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

public class UnconnectedPingPacket extends DataPacket {

	public long pingId;

	public long clientGuid;

	public UnconnectedPingPacket() {
		super(RakNetPacketIds.UNCONNECTED_PING);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeLong(pingId);
		encoder.writeMagic();
		encoder.writeLong(clientGuid);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		pingId = decoder.readLong();
		decoder.skipMagic();
		clientGuid = decoder.readLong();
	}

	@Override
	public String toString() {
		return "UnconnectedPingPacket{" +
				"pingId=" + pingId +
				", clientGuid=" + clientGuid +
				'}';
	}
}
