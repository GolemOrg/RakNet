package net.golem.raknet.protocol.connected;

import net.golem.raknet.RakNetAddressUtils;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.enums.AddressCount;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

import java.net.InetSocketAddress;

public class NewIncomingConnectionPacket extends DataPacket {

	public InetSocketAddress address;

	public InetSocketAddress[] systemAddresses = new InetSocketAddress[AddressCount.MINECRAFT.getCount()];

	public long pingTime;

	public long pongTime;

	public NewIncomingConnectionPacket() {
		super(RakNetPacketIds.NEW_INCOMING_CONNECTION);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeAddress(address);
		for(InetSocketAddress systemAddress : systemAddresses) encoder.writeAddress(systemAddress);
		encoder.writeLong(pingTime);
		encoder.writeLong(pongTime);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		address = decoder.readAddress();

		int offset = decoder.writerIndex() - 16;

		for(int i = 0; i < systemAddresses.length; ++i) {
			systemAddresses[i] = decoder.readerIndex() >= offset ? RakNetAddressUtils.SYSTEM_ADDRESS : decoder.readAddress();
		}

		pingTime = decoder.readLong();
		pongTime = decoder.readLong();
	}

	@Override
	public String toString() {
		return "NewIncomingConnectionPacket{" +
				"packetId=" + packetId +
				'}';
	}
}
