package net.golem.raknet;

import java.net.InetSocketAddress;

public interface RakNetServerListener {

	void openSession(InetSocketAddress address);

	void closeSession(InetSocketAddress address);

	void updatePing(InetSocketAddress address);

}
