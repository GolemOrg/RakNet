package net.golem.raknet.enums;

public enum BitFlags {

	VALID((byte) 0x80),
	ACK((byte) 0x40),
	NAK((byte) 0x20),

	PACKET_PAIR((byte) 0x10),
	CONTINUOUS_PAIR((byte )0x08),
	NEEDS_B_AND_AS((byte) 0x04);

	private byte id;

	BitFlags(byte id) {
		this.id = id;
	}

	public byte getId() {
		return id;
	}
}
