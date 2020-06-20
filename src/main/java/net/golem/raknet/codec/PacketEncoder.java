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

	private ByteBuf buffer = Unpooled.buffer();

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

	public void writeString(String value) {
		buffer.writeShort(value.length());
		buffer.writeCharSequence(value, StandardCharsets.UTF_8);
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

	public void writeUnsigned(long value) {
		while((value & ~0x7FL) != 0) {
			buffer.writeByte(((int) value & 0x7F) | 0x80);
			value >>>= 7;
		}
		buffer.writeByte((int) value);
	}

	public void writeUnsignedVarInt(int value) {
		writeUnsigned(value & 0xFFFFFFFFL);
	}

	public void writeSignedVarInt(int value) {
		writeUnsigned((value << 1) ^ (value >> 31));
	}

	public void writeMagic() {
		buffer.writeBytes(RakNetConstants.MAGIC);
	}

}
