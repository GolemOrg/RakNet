package net.golem.raknet.protocol;

import io.netty.buffer.ByteBuf;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;

public class RawPacket extends DataPacket {

	public ByteBuf buffer;

	public RawPacket(int packetId) {
		super(packetId);
	}

	@Override
	public void encode(PacketEncoder encoder) {}

	@Override
	public void decode(PacketDecoder decoder) {
		buffer = decoder.readRemaining();
	}

	@Override
	public String toString() {
		return "RawPacket{" +
				"packetId= " + packetId +
				"buffer=" + buffer.toString() +
				'}';
	}
}
