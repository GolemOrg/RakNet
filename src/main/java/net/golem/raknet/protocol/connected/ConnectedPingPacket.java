package net.golem.raknet.protocol.connected;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

public class ConnectedPingPacket extends DataPacket {

	public long pingTime;

	public ConnectedPingPacket() {
		super(RakNetPacketIds.CONNECTED_PING);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeLong(pingTime);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		pingTime = decoder.readLong();
	}
}
