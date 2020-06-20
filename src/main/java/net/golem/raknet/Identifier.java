package net.golem.raknet;

import java.util.ArrayList;

public abstract class Identifier {

	private static String HEADER = "MCPE";
	private static char SEPARATOR = ';';

	public abstract ArrayList<Object> getValues();

	public String build() {
		StringBuilder builder = new StringBuilder();
		builder.append(HEADER).append(SEPARATOR);
		getValues().forEach(value -> builder.append(value).append(SEPARATOR));
		return builder.toString();
	}
}
