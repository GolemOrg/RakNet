package net.golem.raknet.protocol.connected;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

public class DisconnectionNotificationPacket extends DataPacket {

	public DisconnectionNotificationPacket() {
		super(RakNetPacketIds.DISCONNECTION_REQUEST);
	}

	@Override
	public void encode(PacketEncoder encoder) {}

	@Override
	public void decode(PacketDecoder decoder) {}
}
