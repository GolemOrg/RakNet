package net.golem.raknet.protocol.connected;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

public class ConnectedPongPacket extends DataPacket {

	public long pingTime;

	public long pongTime;

	public ConnectedPongPacket() {
		super(RakNetPacketIds.CONNECTED_PONG);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeLong(pingTime);
		encoder.writeLong(pongTime);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		pingTime = decoder.readLong();
		pongTime = decoder.readLong();
	}

	@Override
	public String toString() {
		return "ConnectedPongPacket{" +
				"pingTime=" + pingTime +
				", pongTime=" + pongTime +
				'}';
	}
}
