package net.golem.raknet;

import java.util.ArrayList;
import java.util.Arrays;

public class ServerIdentifier extends Identifier {

	@Override
	public ArrayList<Object> getValues() {
		return new ArrayList<>(Arrays.asList(
				"Test Server Name",
				390,
				"1.14.60",
				0,
				100
		));
	}
}
