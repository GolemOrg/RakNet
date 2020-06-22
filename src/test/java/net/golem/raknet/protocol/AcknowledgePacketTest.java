package net.golem.raknet.protocol;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

@Log4j2
public class AcknowledgePacketTest {

	public static void main(String[] args) {
		TreeSet<Integer> records = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));

		ArrayList<Record> compressed = Record.compress(records);
		log.info("Compressed Records: {}", records);
		TreeSet<Integer> decompressed = Record.decompress(compressed);
		log.info("Decompressed Records: {}", decompressed);
		log.info("Sets equals: {}", decompressed.equals(records));
	}
}
