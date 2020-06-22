package net.golem.raknet.protocol.datagram;

public class SplitPacketInfo {

	public short splitId;

	public int splitIndex;

	public int splitCount;

	public SplitPacketInfo(short splitId, int splitIndex, int splitCount) {
		this.splitId = splitId;
		this.splitIndex = splitIndex;
		this.splitCount = splitCount;
	}

	public short getSplitId() {
		return splitId;
	}

	public int getSplitIndex() {
		return splitIndex;
	}

	public int getSplitCount() {
		return splitCount;
	}

	@Override
	public String toString() {
		return "SplitPacketInfo{" +
				"splitId=" + splitId +
				", splitIndex=" + splitIndex +
				", splitCount=" + splitCount +
				'}';
	}
}
