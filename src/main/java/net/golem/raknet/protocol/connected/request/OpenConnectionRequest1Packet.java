package net.golem.raknet.protocol.connected.request;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

public class OpenConnectionRequest1Packet extends DataPacket {

	public static final int MTU_PADDING = 28;

	public byte networkProtocol;

	public short maximumTransferUnits;

	public OpenConnectionRequest1Packet() {
		super(RakNetPacketIds.OPEN_CONNECTION_REQUEST_1);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeMagic();
		encoder.writeByte(networkProtocol);
		// write mtu
	}

	@Override
	public void decode(PacketDecoder decoder) {
		decoder.skipMagic();
		this.networkProtocol = decoder.readByte();
		this.maximumTransferUnits = (short) (decoder.readableBytes() + MTU_PADDING);
		decoder.skipReadable();
	}
}
