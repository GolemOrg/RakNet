package net.golem.raknet.protocol;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

@Log4j2
public class AcknowledgePacket extends DataPacket {

	public static final int RECORD_TYPE_RANGE = 0;
	public static final int RECORD_TYPE_SINGLE = 1;

	public Set<Integer> records = new TreeSet<>();

	public AcknowledgePacket(int id) {
		super(id);
	}

	public static AcknowledgePacket createACK() {
		return new AcknowledgePacket(RakNetPacketIds.ACK);
	}

	public static AcknowledgePacket createNAK() {
		return new AcknowledgePacket(RakNetPacketIds.NAK);
	}

	@Override
	public void decode(PacketDecoder decoder) {
		records = new TreeSet<>();
		int count = decoder.readUnsignedShort();
		for(int i = 0; i < count; i++) {
			int type = decoder.readByte();
			if(type == RECORD_TYPE_SINGLE) {
				records.add(decoder.readUnsignedMediumLE());
			} else {
				int startIndex = decoder.readUnsignedMediumLE();
				int endIndex = decoder.readUnsignedMediumLE();
				for(int index = startIndex; index <= endIndex; index++) {
					records.add(index);
				}
			}
		}
	}

	public void pmmpEncode(PacketEncoder encoder) {
		ByteBuf buffer = Unpooled.buffer();

		short recordCount = 0;

		Integer[] records = this.records.toArray(new Integer[0]);
		Arrays.sort(records);
		int count = records.length;

		if(count > 0) {
			int pointer = 1;
			int start = records[0];
			int last = records[0];
			while(pointer < count) {
				int current = records[pointer++];
				int diff = current - last;
				if(diff == 1) {
					last = current;
				} else if(diff > 1) {
					if(start == last) {
						buffer.writeByte(RECORD_TYPE_SINGLE);
						buffer.writeMediumLE(start);
						start = last = current;
					} else {
						buffer.writeByte(RECORD_TYPE_RANGE);
						buffer.writeMediumLE(start);
						buffer.writeMediumLE(last);
						start = last = current;
					}
					++recordCount;
				}
			}
		}
		encoder.writeShort(recordCount);
		encoder.writeBytes(buffer);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		ByteBuf buffer = Unpooled.buffer();
		int recordCount = 0;
		if (records.size() > 0) {
			PeekingIterator<Integer> iterator = Iterators.peekingIterator(records.iterator());
			while (iterator.hasNext()) {
				int current = iterator.next();
				if(iterator.hasNext() && (iterator.peek() - current == 1)) {
					iterator.next();

					continue;
				}
				boolean single = !iterator.hasNext() || (iterator.peek() - current == 1);
				buffer.writeByte(single ? RECORD_TYPE_SINGLE : RECORD_TYPE_RANGE);
				buffer.writeMediumLE(current);
				if (!single) buffer.writeMediumLE(iterator.next());
				++recordCount;
			}
		}
		encoder.writeShort((short) recordCount);
		encoder.writeBytes(buffer);
	}

	public boolean isACK() {
		return getPacketId() == RakNetPacketIds.ACK;
	}

	public boolean isNAK() {
		return getPacketId() == RakNetPacketIds.NAK;
	}

	@Override
	public String toString() {
		return "AcknowledgePacket{" +
				"records=" + records +
				'}';
	}
}
