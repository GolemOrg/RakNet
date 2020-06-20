package net.golem.raknet.protocol.connected.reply;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

public class OpenConnectionReply1Packet extends DataPacket {

	public long guid;

	public boolean useSecurity = false;

	public short maximumTransferUnits;

	public OpenConnectionReply1Packet() {
		super(RakNetPacketIds.OPEN_CONNECTION_REPLY_1);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeMagic();
		encoder.writeLong(guid);
		encoder.writeBoolean(useSecurity);
		encoder.writeShort(maximumTransferUnits);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		decoder.skipMagic();
		guid = decoder.readLong();
		useSecurity = decoder.readBoolean();
		maximumTransferUnits = decoder.readShort();
	}
}
