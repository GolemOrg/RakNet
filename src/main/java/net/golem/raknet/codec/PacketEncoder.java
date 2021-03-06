package net.golem.raknet.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import net.golem.raknet.RakNetAddressUtils;
import net.golem.raknet.RakNetConstants;

public class PacketEncoder {

	public static final int VAR_STRING = 0;
	public static final int SHORT_STRING = 1;

	private final ByteBuf buffer = Unpooled.buffer().retain();

	public ByteBuf getBuffer() {
		return buffer;
	}

	public void writeBoolean(boolean value) {
		buffer.writeBoolean(value);
	}

	public void writeByte(int value) {
		buffer.writeByte(value);
	}

	public void writeBytes(byte[] value) {
		buffer.writeBytes(value);
	}

	public void writeBytes(ByteBuf value) {
		buffer.writeBytes(value);
	}

	public void writeShort(short value) {
		buffer.writeShort(value);
	}

	public void writeShortLE(short value) {
		buffer.writeShortLE(value);
	}

	public void writeInt(int value) {
		buffer.writeInt(value);
	}

	public void writeIntLE(int value) {
		buffer.writeIntLE(value);
	}

	public void writeMedium(int value) {
		buffer.writeMedium(value);
	}

	public void writeMediumLE(int value) {
		buffer.writeMediumLE(value);
	}

	public void writeLong(long value) {
		buffer.writeLong(value);
	}

	public void writeLongLE(long value) {
		buffer.writeLongLE(value);
	}

	public void writeFloat(float value) {
		buffer.writeFloat(value);
	}

	public void writeFloatLE(float value) {
		buffer.writeFloatLE(value);
	}

	public void writeDouble(double value) {
		buffer.writeDouble(value);
	}

	public void writeDoubleLE(double value) {
		buffer.writeDoubleLE(value);
	}

	public void writeString(String value, int type) {
		if(type == SHORT_STRING) {
			buffer.writeShort(value.length());
		} else {
			writeUnsignedVarInt(value.length());
		}
		buffer.writeCharSequence(value, StandardCharsets.UTF_8);
	}

	public void writeString(String value) {
		writeString(value, VAR_STRING);
	}

	private void flip(ByteBuf buffer) {
		for(int i = 0; i < buffer.capacity(); i++) {
			buffer.setByte(i, ~(buffer.getByte(i) & 0xFF));
		}
	}

	public void writeAddress(InetSocketAddress address) {
		try {
			if(address == null || address.getAddress() == null) {
				throw new NullPointerException("Address or IP address null");
			}
			ByteBuf addressBytes = Unpooled.copiedBuffer(address.getAddress().getAddress());
			this.writeByte((byte) RakNetAddressUtils.getAddressVersion(address.getAddress()));
			if(address.getAddress() instanceof Inet4Address) {
				flip(addressBytes);
				writeBytes(addressBytes);
				writeShort((short) address.getPort());
			} else if(address.getAddress() instanceof Inet6Address) {
				writeByte((byte) RakNetAddressUtils.AF_INET6);
				writeShortLE((short) address.getPort());
				writeInt(0x00); // Flow info
				writeBytes(addressBytes);
				writeInt(((Inet6Address) address.getAddress()).getScopeId());
			} else {
				throw new Exception("Unknown InetAddress type");
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	private void writeUnsigned(long value) {
		while((value & ~0x7FL) != 0) {
			buffer.writeByte(((byte) value & 0x7F) | 0x80);
			value >>= 7;
		}
		buffer.writeByte((byte) value);
	}

	public void writeUnsignedVarInt(int value) {
		writeUnsigned(value);
	}

	public void writeSignedVarInt(int value) {
		writeUnsigned((value << 1) ^ (value >> 31));
	}

	public void writeUnsignedVarLong(long value) {
		writeUnsigned(value);
	}

	public void writeSignedVarLong(long value) {
		writeUnsigned((value << 1) ^ (value >> 63));
	}

	public void writeMagic() {
		buffer.writeBytes(RakNetConstants.MAGIC);
	}

}
