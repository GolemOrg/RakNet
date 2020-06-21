package net.golem.raknet.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.utils.ByteBufUtils;

import java.util.Arrays;
import java.util.TreeSet;

@Log4j2
public class AcknowledgePacketTest {

	public static void main(String[] args) {
		TreeSet<Integer> ackQueue = new TreeSet<>(Arrays.asList(1, 2, 3, 5));

		ByteBuf encodeBuffer = encode(ackQueue);
		log.info("Encode buffer: ");
		log.info(ByteBufUtils.toString(encodeBuffer));

		ByteBuf pmmpBuffer = encodePMMP(ackQueue);

		log.info("PMMP buffer: ");
		log.info(ByteBufUtils.toString(pmmpBuffer));

		log.info("Match: {}", Arrays.equals(ByteBufUtils.array(encodeBuffer), ByteBufUtils.array(pmmpBuffer)));
	}

	public static ByteBuf encode(TreeSet<Integer> records) {
		AcknowledgePacket pk = AcknowledgePacket.createACK();
		pk.records = records;
		PacketEncoder encoder = new PacketEncoder();
		pk.encode(encoder);
		return encoder.getBuffer();
	}

	public static ByteBuf encodePMMP(TreeSet<Integer> records) {
		AcknowledgePacket pk = AcknowledgePacket.createACK();
		pk.records = records;
		PacketEncoder encoder = new PacketEncoder();
		pk.pmmpEncode(encoder);
		return encoder.getBuffer();
	}
}
