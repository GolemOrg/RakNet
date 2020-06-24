package net.golem.raknet.codec;

import io.netty.buffer.ByteBuf;
import net.golem.raknet.RakNetAddressUtils;
import net.golem.raknet.RakNetConstants;
import net.golem.raknet.utils.ByteBufUtils;

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

	public float readFloat() {
		return buffer.readFloat();
	}

	public float readFloatLE() {
		return buffer.readFloatLE();
	}

	public double readDouble() {
		return buffer.readDouble();
	}

	public double readDoubleLE() {
		return buffer.readDoubleLE();
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

	public void resetReaderIndex() {
		buffer.resetReaderIndex();
	}

	public int writerIndex() {
		return buffer.writerIndex();
	}

	public int readableBytes() {
		return buffer.readableBytes();
	}

	public String readString() {
		int length = readUnsignedVarInt();
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
				port = this.readUnsignedShort();
			} else if(type == RakNetAddressUtils.IPV6) {
				this.readShort(); // AF_INET6
				port = this.readUnsignedShort();
				this.readInt(); // Flow Info
				addressBytes = ByteBufUtils.array(this.readBytes(RakNetAddressUtils.IPV6_ADDRESS_LENGTH));
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

	public int readUnsignedVarInt() {
		int value = 0;
		int size = 0;
		int b;
		while (((b = buffer.readByte()) & 0x80) == 0x80) {
			value |= (b & 0x7F) << (size++ * 7);
			if (size >= 5) {
				throw new IllegalArgumentException("VarInt too big");
			}
		}
		return value | ((b & 0x7F) << (size * 7));
	}

	public int readSignedVarInt() {
		int value = readUnsignedVarInt();
		return (value >>> 1) ^ -(value & 1);
	}

	public long readUnsignedVarLong() {
		return readUnsignedVarInt();
	}

	public long readSignedVarLong() {
		return readSignedVarInt();
	}

}
