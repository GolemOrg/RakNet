package net.golem.raknet.protocol.datagram;

import io.netty.buffer.ByteBuf;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.enums.PacketReliability;

@Log4j2
public class EncapsulatedPacket {

	private static final int RELIABILITY_SHIFT = 5;
	private static final int RELIABILITY_FLAGS = 0b111 << RELIABILITY_SHIFT;

	private static final int SPLIT_FLAG = 0b00010000;

	public PacketReliability reliability;

	public int messageIndex = -1;
	public int sequenceIndex = -1;

	public int orderIndex = -1;
	public int orderChannel = -1;

	public SplitPacketInfo splitInfo;

	public ByteBuf buffer;

	public int identifierACK = -1;

	public void encode(PacketEncoder encoder) {
		int flags = reliability.getId() << RELIABILITY_SHIFT;
		if(splitInfo != null) {
			flags |= SPLIT_FLAG;
		}
		encoder.writeByte(flags);
		encoder.writeShort((short) (buffer.writerIndex() << 3));

		if(reliability.isReliable()) {
			encoder.writeMediumLE(messageIndex);
		}

		if(reliability.isSequenced()) {
			encoder.writeMediumLE(sequenceIndex);
		}

		if(reliability.isSequenced() || reliability.isOrdered()) {
			encoder.writeMediumLE(orderIndex);
			encoder.writeByte(orderChannel);
		}

		if(splitInfo != null) {
			encoder.writeInt(splitInfo.splitCount);
			encoder.writeShort(splitInfo.splitId);
			encoder.writeInt(splitInfo.splitIndex);
		}

		encoder.writeBytes(buffer);

		try {
			buffer.release();
		} catch (Exception ignored) {}
	}


	public void decode(PacketDecoder decoder) {
		int flags = decoder.readUnsignedByte();
		reliability = PacketReliability.from((flags & RELIABILITY_FLAGS) >> RELIABILITY_SHIFT);

		boolean split = (flags & SPLIT_FLAG) > 0;

		int length = (int) Math.ceil(decoder.readShort() / (float) Byte.SIZE);

		if(length == 0) {
			log.error("Encapsulated payload length cannot be zero");
			return;
		}

		if(reliability.isReliable()) {
			messageIndex = decoder.readMediumLE();
		}

		if(reliability.isSequenced()) {
			sequenceIndex = decoder.readMediumLE();
		}

		if(reliability.isSequenced() || reliability.isOrdered()) {
			orderIndex = decoder.readMediumLE();
			orderChannel = decoder.readByte();
		}

		if(split) {
			int splitCount = decoder.readInt();
			short splitId = decoder.readShort();
			int splitIndex = decoder.readInt();
			splitInfo = new SplitPacketInfo(splitId, splitIndex, splitCount);
		}

		buffer = decoder.readSlice(length).retain();
	}

	public int length() {
		return
			1 + // reliability
			2 + // length
			(reliability.isReliable() ? 3 : 0) + // message index
			(reliability.isSequenced() ? 3 : 0) + // sequence index
			(reliability.isSequenced() || reliability.isOrdered() ? 3 + 1 : 0) + // order index (3) + order channel (1)
			(splitInfo != null ? 4 + 2 + 4 : 0) + // split count (4) + split ID (2) + split index (4)
			buffer.writerIndex();
	}

	@Override
	public String toString() {
		return "EncapsulatedPacket{" +
				"reliability=" + reliability +
				", messageIndex=" + messageIndex +
				", sequenceIndex=" + sequenceIndex +
				", orderIndex=" + orderIndex +
				", orderChannel=" + orderChannel +
				", splitInfo=" + splitInfo +
				", buffer=" + buffer +
				", identifierACK=" + identifierACK +
				'}';
	}
}
