package net.golem.raknet.session;

import io.netty.buffer.Unpooled;
import net.golem.raknet.protocol.datagram.EncapsulatedPacket;
import net.golem.raknet.session.codec.PacketDecodeLayer;

import java.util.Arrays;
import java.util.LinkedHashMap;

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

		for(EncapsulatedPacket split : packets) {
			if(split == null) {
				splitPackets.put(id, packets);
				return null;
			}
		}
		EncapsulatedPacket assembled = new EncapsulatedPacket();
		assembled.buffer = Unpooled.buffer();
		assembled.reliability = packet.reliability;
		assembled.messageIndex = packet.messageIndex;
		assembled.sequenceIndex = packet.sequenceIndex;
		assembled.orderIndex = packet.orderIndex;
		assembled.orderChannel = packet.orderChannel;

		Arrays.asList(packets).forEach(pk -> {
			try {
				assembled.buffer.writeBytes(pk.buffer);
			} finally {
				pk.buffer.release();
			}
		});
		splitPackets.remove(id);
		return assembled;
	}

}
