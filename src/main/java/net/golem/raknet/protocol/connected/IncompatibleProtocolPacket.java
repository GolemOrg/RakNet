package net.golem.raknet.protocol.connected;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

public class IncompatibleProtocolPacket extends DataPacket {

	public int protocol;

	public long guid;

	public IncompatibleProtocolPacket() {
		super(RakNetPacketIds.INCOMPATIBLE_PROTOCOL_VERSION);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeByte(protocol);
		encoder.writeMagic();
		encoder.writeLong(guid);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		protocol = decoder.readUnsignedByte();
		decoder.skipMagic();
		guid = decoder.readLong();
	}
}
