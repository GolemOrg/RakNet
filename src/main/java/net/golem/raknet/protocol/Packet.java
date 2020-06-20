package net.golem.raknet.protocol;

import io.netty.buffer.ByteBuf;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;

public interface Packet {

	void encode(PacketEncoder encoder);

	void decode(PacketDecoder decoder);

	ByteBuf write(PacketEncoder encoder);

	void read(PacketDecoder decoder);

}
