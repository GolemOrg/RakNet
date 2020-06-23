package net.golem.raknet.protocol.acknowledge;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AcknowledgePacket extends DataPacket {

	public Set<Integer> records = new HashSet<>();

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
		records.clear();
		int count = decoder.readUnsignedShort();
		ArrayList<Record> recordList = new ArrayList<>();
		for(int i = 0; i < count; i++) {
			int type = decoder.readByte();
			int start = decoder.readMediumLE();
			int end = start;
			if(type == Record.TYPE_RANGE) end = decoder.readMediumLE();
			recordList.add(new Record(start, end));
		}
		records = Record.decompress(recordList);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		ArrayList<Record> recordList = Record.compress(records);
		encoder.writeShort((short) recordList.size());
		recordList.forEach(record -> record.encode(encoder));
	}

	public boolean isACK() {
		return packetId == RakNetPacketIds.ACK;
	}

	public boolean isNAK() {
		return packetId == RakNetPacketIds.NAK;
	}

	@Override
	public String toString() {
		return "AcknowledgePacket{" +
				"records=" + records +
				'}';
	}
}