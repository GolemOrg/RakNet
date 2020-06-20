package net.golem.raknet.protocol.connected.reply;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

import java.net.InetSocketAddress;

public class OpenConnectionReply2Packet extends DataPacket {

	public long serverGuid;

	public InetSocketAddress clientAddress;

	public int maximumTransferUnits;

	public boolean serverSecurity = false;

	public OpenConnectionReply2Packet() {
		super(RakNetPacketIds.OPEN_CONNECTION_REPLY_2);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeMagic();
		encoder.writeLong(serverGuid);
		encoder.writeAddress(clientAddress);
		encoder.writeShort((short) maximumTransferUnits);
		encoder.writeBoolean(serverSecurity);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		decoder.skipMagic();
		serverGuid = decoder.readLong();
		clientAddress = decoder.readAddress();
		maximumTransferUnits = decoder.readUnsignedShort();
		serverSecurity = decoder.readBoolean();
	}
}
