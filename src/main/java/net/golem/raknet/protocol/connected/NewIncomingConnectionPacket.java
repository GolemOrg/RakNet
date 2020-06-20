package net.golem.raknet.protocol.connected;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

public class NewIncomingConnectionPacket extends DataPacket {

	public NewIncomingConnectionPacket() {
		super(RakNetPacketIds.NEW_INCOMING_CONNECTION);
	}

	@Override
	public void encode(PacketEncoder encoder) {

	}

	@Override
	public void decode(PacketDecoder decoder) {

	}
}
