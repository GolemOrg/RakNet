package net.golem.raknet.protocol.datagram;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.enums.BitFlags;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

import java.util.ArrayList;

public class RakNetDatagram extends DataPacket {

	public static final int HEADER_SIZE = 1 + 3; // header flags (1) + sequence number (3)

	public int flags;

	public ArrayList<EncapsulatedPacket> packets = new ArrayList<>();

	public int sequenceNumber;

	public RakNetDatagram(int flags) {
		super(RakNetPacketIds.DATAGRAM);
		this.flags = flags;
	}

	public RakNetDatagram() {
		this(0);
	}

	public int getFlags() {
		return flags;
	}

	public ArrayList<EncapsulatedPacket> getPackets() {
		return packets;
	}

	@Override
	public void encodeHeader(PacketEncoder encoder) {
		encoder.writeByte(BitFlags.VALID.getId() | flags);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.getBuffer().writeMediumLE(sequenceNumber);
		for(EncapsulatedPacket packet : packets) {
			packet.encode(encoder);
		}
	}

	@Override
	public void decode(PacketDecoder decoder) {
		decoder.getBuffer().resetReaderIndex();
		this.decodeHeader(decoder);
		this.sequenceNumber = decoder.readUnsignedMediumLE();
		while(decoder.isReadable()) {
			EncapsulatedPacket packet = new EncapsulatedPacket();
			packet.decode(decoder);
			packets.add(packet);
		}
	}

	public int length() {
		int length = HEADER_SIZE;
		for(EncapsulatedPacket packet : packets) {
			length += packet.length();
		}
		return length;
	}
}
