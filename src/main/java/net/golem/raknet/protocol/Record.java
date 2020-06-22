package net.golem.raknet.protocol;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import net.golem.raknet.codec.PacketDecoder;
import net.golem.raknet.codec.PacketEncoder;
import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class Record {

	public static final byte TYPE_RANGE = 0;
	public static final byte TYPE_SINGLE = 1;

	private int start;

	private int end;

	public Record(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public byte getType() {
		return start == end ? TYPE_SINGLE : TYPE_RANGE;
	}


	public boolean isSingle() {
		return getType() == TYPE_SINGLE;
	}

	public boolean isRange() {
		return getType() == TYPE_RANGE;
	}

	public static ArrayList<Record> compress(Set<Integer> records) {
		ArrayList<Record> output = new ArrayList<>();
		PeekingIterator<Integer> iterator = Iterators.peekingIterator(records.iterator());
		if(records.size() > 0) {
			int start = iterator.next();
			int end = start;
			while(iterator.hasNext()) {
				int current = iterator.next();
				int difference = current - end;
				if(difference <= 1) {
					end = current;
					continue;
				}
				output.add(new Record(start, end));
				start = end = current;
			}
			output.add(new Record(start, end));
		}
		return output;
	}

	public static TreeSet<Integer> decompress(ArrayList<Record> records) {
		TreeSet<Integer> output = new TreeSet<>();
		records.forEach(record -> {
			if(record.isRange()) {
				for(int i = record.start; i <= record.end; i++) output.add(i);
			} else {
				output.add(record.start);
			}
		});
		return output;
	}

	public void encode(PacketEncoder encoder) {
		encoder.writeByte(getType());
		encoder.writeMediumLE(getStart());
		if(isRange()) encoder.writeMediumLE(getEnd());
	}

	@Override
	public String toString() {
		return "Record{" +
				"start=" + start +
				", end=" + end +
				'}';
	}
}
