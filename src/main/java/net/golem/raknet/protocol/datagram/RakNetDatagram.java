package net.golem.raknet.protocol.datagram;

import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.enums.BitFlags;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketIds;

import java.util.ArrayList;

public class RakNetDatagram extends DataPacket {

	public int headerFlags;

	public int sequenceIndex;

	public ArrayList<EncapsulatedPacket> packets = new ArrayList<>();

	public RakNetDatagram(int flags) {
		super(RakNetPacketIds.DATAGRAM);
		this.headerFlags = flags;
	}

	public RakNetDatagram() {
		this(0);
	}

	@Override
	public void encodeHeader(PacketEncoder encoder) {
		encoder.writeByte(BitFlags.VALID.getId() | headerFlags);
	}

	@Override
	public void encode(PacketEncoder encoder) {
		encoder.writeMediumLE(sequenceIndex);
		for(EncapsulatedPacket packet : packets) {
			packet.encode(encoder);
		}
	}

	@Override
	public void decodeHeader(PacketDecoder decoder) {
		headerFlags = decoder.readUnsignedByte();
	}

	@Override
	public void decode(PacketDecoder decoder) {
		sequenceIndex = decoder.readUnsignedMediumLE();
		while(decoder.isReadable()) {
			EncapsulatedPacket packet = new EncapsulatedPacket();
			packet.decode(decoder);
			packets.add(packet);
		}
	}

	@Override
	public String toString() {
		return "RakNetDatagram{" +
				"headerFlags=" + headerFlags +
				", sequenceIndex=" + sequenceIndex +
				", packets=" + packets +
				'}';
	}
}
