package net.golem.raknet.protocol.connected.connection;

import net.golem.raknet.RakNetAddressUtils;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.enums.AddressCount;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class ConnectionRequestAcceptedPacket extends DataPacket {

	public InetSocketAddress clientAddress;

	public InetSocketAddress[] addresses = new InetSocketAddress[AddressCount.MINECRAFT.getCount()];

	public long pingTime;
	public long pongTime;

	public ConnectionRequestAcceptedPacket() {
		super(RakNetPacketIds.CONNECTION_REQUEST_ACCEPTED);
		addresses[0] = RakNetAddressUtils.SYSTEM_ADDRESS;
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeAddress(clientAddress);
		encoder.writeShort((short) 0); // system index
		for(InetSocketAddress address : addresses) {
			InetSocketAddress current = address != null ? address : RakNetAddressUtils.SYSTEM_ADDRESS;
			encoder.writeAddress(current);
		}
		encoder.writeLong(pingTime);
		encoder.writeLong(pongTime);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		clientAddress = decoder.readAddress();
		decoder.readShort(); // system index
		for(int i = 0; i < addresses.length; ++i) {
			this.addresses[i] = decoder.readerIndex() + 16 < decoder.readableBytes() ? decoder.readAddress() : RakNetAddressUtils.SYSTEM_ADDRESS;
		}
		pingTime = decoder.readLong();
		pongTime = decoder.readLong();
	}

	@Override
	public String toString() {
		return "ConnectionRequestAcceptedPacket{" +
				"clientAddress=" + clientAddress +
				", addresses=" + Arrays.toString(addresses) +
				", pingTime=" + pingTime +
				", pongTime=" + pongTime +
				'}';
	}
}
