package net.golem.raknet.session;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.protocol.datagram.EncapsulatedPacket;
import net.golem.raknet.session.codec.PacketDecodeLayer;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;

@Log4j2
public class EncapsulatedSplitHandler {

	private LinkedHashMap<Short, EncapsulatedPacket[]> splitPackets = new LinkedHashMap<>();

	private PacketDecodeLayer decodeLayer;

	public EncapsulatedSplitHandler(PacketDecodeLayer decodeLayer) {
		this.decodeLayer = decodeLayer;
	}

	public PacketDecodeLayer getDecodeLayer() {
		return decodeLayer;
	}

	public void close() {
		splitPackets.clear();
	}

	public EncapsulatedPacket handle(EncapsulatedPacket packet) {
		short id = packet.splitInfo.splitId;
		int index = packet.splitInfo.splitIndex;

		EncapsulatedPacket[] packets = splitPackets.getOrDefault(id, new EncapsulatedPacket[packet.splitInfo.splitCount]);
		packets[index] = packet;
		packet.buffer.retain();
		if(Arrays.stream(packets).filter(Objects::nonNull).count() < packet.splitInfo.splitCount) {
			splitPackets.put(id, packets);
			return null;
		}
		EncapsulatedPacket assembled = new EncapsulatedPacket();
		assembled.buffer = Unpooled.buffer();
		assembled.reliability = packet.reliability;
		assembled.messageIndex = packet.messageIndex;
		assembled.sequenceIndex = packet.sequenceIndex;
		assembled.orderIndex = packet.orderIndex;
		assembled.orderChannel = packet.orderChannel;

		Arrays.stream(packets).forEach(split -> {
			split.buffer.resetReaderIndex();
			assembled.buffer.writeBytes(split.buffer);
			try {
				split.buffer.release();
			} catch (Exception ignored) {}
		});
		splitPackets.remove(id);
		return assembled;
	}

}
