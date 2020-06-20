package net.golem.raknet.codec;

import io.netty.buffer.ByteBuf;
import net.golem.raknet.RakNetAddressUtils;
import net.golem.raknet.RakNetConstants;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class PacketDecoder {

	private ByteBuf buffer;

	public PacketDecoder(ByteBuf buffer) {
		this.buffer = buffer;
	}

	public ByteBuf getBuffer() {
		return buffer;
	}

	public boolean readBoolean() {
		return buffer.readBoolean();
	}

	public byte readByte() {
		return buffer.readByte();
	}

	public short readUnsignedByte() {
		return buffer.readUnsignedByte();
	}

	public ByteBuf readBytes(int length) {
		return buffer.readBytes(length);
	}

	public short readShort() {
		return buffer.readShort();
	}

	public short readShortLE() {
		return buffer.readShortLE();
	}

	public int readUnsignedShort() {
		return buffer.readUnsignedShort();
	}

	public int readUnsignedShortLE() {
		return buffer.readUnsignedShortLE();
	}

	public int readMedium() {
		return buffer.readMedium();
	}

	public int readMediumLE() {
		return buffer.readMediumLE();
	}

	public int readUnsignedMedium() {
		return buffer.readUnsignedMedium();
	}

	public int readUnsignedMediumLE() {
		return buffer.readUnsignedMediumLE();
	}

	public long readLong() {
		return buffer.readLong();
	}

	public long readLongLE() {
		return buffer.readLongLE();
	}

	public int readInt() {
		return buffer.readInt();
	}

	public int readIntLE() {
		return buffer.readIntLE();
	}

	public long readUnsignedInt() {
		return buffer.readUnsignedInt();
	}

	public ByteBuf readSlice(int length) {
		return buffer.readSlice(length);
	}

	public boolean isReadable() {
		return buffer.isReadable();
	}

	public ByteBuf readRemaining() {
		return buffer.readBytes(buffer.readableBytes());
	}

	public int readerIndex() {
		return buffer.readerIndex();
	}

	public int readableBytes() {
		return buffer.readableBytes();
	}

	public String readString() {
		int length = readShort();
		return (String) buffer.readCharSequence(length, StandardCharsets.UTF_8);
	}

	public void skipMagic() {
		buffer.skipBytes(RakNetConstants.MAGIC.length);
	}

	public void skipReadable() {
		buffer.skipBytes(readableBytes());
	}

	public InetSocketAddress readAddress() {
		try {
			int type = this.readByte();
			byte[] addressBytes;
			int port;
			if(type == RakNetAddressUtils.IPV4) {
				addressBytes = new byte[RakNetAddressUtils.IPV4_ADDRESS_LENGTH];
				for(int i = 0; i < addressBytes.length; i++) addressBytes[i] = (byte) (~this.readByte() & 0xFF);
				port = this.readShort();
			} else if(type == RakNetAddressUtils.IPV6) {
				this.readShort(); // AF_INET6
				port = this.readShort();
				this.readInt(); // Flow Info
				addressBytes = this.readBytes(RakNetAddressUtils.IPV6_ADDRESS_LENGTH).array();
				this.readInt(); // Scope ID
			} else {
				throw new Exception(String.format("Unknown address type %s", type));
			}
			return new InetSocketAddress(InetAddress.getByAddress(addressBytes), port);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}

}
