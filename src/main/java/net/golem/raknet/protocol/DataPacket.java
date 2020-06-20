package net.golem.raknet.protocol;

import io.netty.buffer.ByteBuf;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;

public abstract class DataPacket implements Packet {

	protected int packetId;

	public DataPacket(int packetId) {
		this.packetId = packetId;
	}

	public int getPacketId() {
		return packetId;
	}

	public void encodeHeader(PacketEncoder encoder) {
		encoder.writeByte(this.getPacketId());
	}

	public void decodeHeader(PacketDecoder decoder) {
		packetId = decoder.readByte();
	}

	@Override
	public ByteBuf write(PacketEncoder encoder) {
		this.encodeHeader(encoder);
		this.encode(encoder);
		return encoder.getBuffer();
	}

	public void read(PacketDecoder decoder) {
		this.decodeHeader(decoder);
		this.decode(decoder);
	}

	public ByteBuf create() {
		return write(new PacketEncoder());
	}
}
