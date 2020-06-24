package net.golem.raknet.protocol.unconnected;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

public class UnconnectedPongPacket extends DataPacket {

	public long pingId;

	public long guid;

	public String serverName;

	public UnconnectedPongPacket() {
		super(RakNetPacketIds.UNCONNECTED_PONG);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeLong(pingId);
		encoder.writeLong(guid);
		encoder.writeMagic();
		encoder.writeString(serverName, PacketEncoder.SHORT_STRING);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		pingId = decoder.readLong();
		guid = decoder.readLong();
		decoder.skipMagic();
		serverName = decoder.readString();
	}
}
