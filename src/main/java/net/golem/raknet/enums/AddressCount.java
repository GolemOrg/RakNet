package net.golem.raknet.enums;

public enum AddressCount {

	RAKNET(10),
	MINECRAFT(20);

	private int count;

	AddressCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}
}
