package net.golem.raknet.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public final class ByteBufUtils {

	public static byte[] array(ByteBuf buffer) {
		return ByteBufUtil.getBytes(buffer);
	}

	public static String convert(byte[] buffer) {
		StringBuilder builder = new StringBuilder();
		for(byte current : buffer) builder.append(String.format("%02X ", current));
		return builder.toString();
	}

	public static String toString(ByteBuf buffer) {
		return convert(array(buffer));
	}
}
