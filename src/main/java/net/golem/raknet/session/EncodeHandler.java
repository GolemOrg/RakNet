package net.golem.raknet.session;

import io.netty.buffer.ByteBuf;
import net.golem.raknet.enums.PacketReliability;
import net.golem.raknet.protocol.datagram.EncapsulatedPacket;
import net.golem.raknet.protocol.datagram.RakNetDatagram;
import net.golem.raknet.protocol.datagram.SplitPacketInfo;
import net.golem.raknet.session.codec.PacketEncodeLayer;
import net.golem.raknet.utils.ByteBufUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class EncodeHandler {

	private PacketEncodeLayer encodeLayer;

	public LinkedHashMap<Integer, ArrayList<Integer>> requiresAck = new LinkedHashMap<>();

	private short currentSplitId = 0;

	private int currentMessageIndex = 0;
	private int currentOrderIndex = 0;
	private int currentSequenceIndex = 0;

	public EncodeHandler(PacketEncodeLayer encodeLayer) {
		this.encodeLayer = encodeLayer;
	}

	public PacketEncodeLayer getEncodeLayer() {
		return encodeLayer;
	}

	public void passEncapsulated(EncapsulatedPacket packet, boolean immediate) {
		if(packet.identifierACK != -1) {
			requiresAck.put(packet.identifierACK, new ArrayList<>());
		}

		PacketReliability reliability = packet.reliability;
		if(reliability.isOrdered()) {
			packet.orderIndex = currentOrderIndex++;
		} else if(reliability.isSequenced()) {
			packet.orderIndex = currentOrderIndex++;
			packet.sequenceIndex = currentSequenceIndex++;
		}

		int maxSize = encodeLayer.getSession().getMaximumTransferUnits() - PacketEncodeLayer.ENCAPSULATED_OVERHEAD;
		if(packet.buffer.writerIndex() > maxSize) {
			int bufferSize = packet.buffer.writerIndex() / maxSize;
			ArrayList<ByteBuf> buffers = ByteBufUtils.split(packet.buffer, bufferSize);
			short splitId = currentSplitId++;
			for(int i = 0; i < buffers.size(); i++) {
				EncapsulatedPacket split = new EncapsulatedPacket();
				split.splitInfo = new SplitPacketInfo(splitId, i, buffers.size());
				split.reliability = packet.reliability;
				split.buffer = buffers.get(i);

				if(split.reliability.isReliable()) {
					split.messageIndex = this.currentMessageIndex++;
				}

				split.sequenceIndex = packet.sequenceIndex;

				split.orderIndex = packet.orderIndex;
				split.orderChannel = packet.orderChannel;

				addEncapsulatedToQueue(split, true);
			}
		} else {
			if(packet.reliability.isReliable()) {
				packet.messageIndex = this.currentMessageIndex++;
			}
			addEncapsulatedToQueue(packet);
		}
	}

	public void passEncapsulated(EncapsulatedPacket packet) {
		passEncapsulated(packet, false);
	}


	public void checkQueueSize(EncapsulatedPacket packet) {
		int length = encodeLayer.packetQueue.stream().mapToInt(EncapsulatedPacket::length).sum() + RakNetDatagram.HEADER_SIZE + packet.length();
		if(length > encodeLayer.getSession().getMaximumTransferUnits() - PacketEncodeLayer.LENGTH_OVERHEAD) {
			encodeLayer.sendQueue();
		}
	}

	public void addEncapsulatedToQueue(EncapsulatedPacket packet, boolean immediate) {
		if(packet.identifierACK != -1 && packet.messageIndex != -1) {
			requiresAck.put(packet.identifierACK, new ArrayList<>(Collections.singletonList(packet.messageIndex)));
		}
		checkQueueSize(packet);
		encodeLayer.packetQueue.add(packet);
		if(immediate) encodeLayer.sendQueue();
	}

	public void addEncapsulatedToQueue(EncapsulatedPacket packet) {
		addEncapsulatedToQueue(packet, false);
	}
}
