package net.golem.raknet;

public class RakNetServerTest {

	public static void main(String[] args) {
		new RakNetServer("0.0.0.0", 19132, new ServerListener(), new ServerIdentifier(), true);
	}
}
