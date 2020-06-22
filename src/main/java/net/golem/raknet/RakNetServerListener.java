package net.golem.raknet;

public interface RakNetServerListener {


	void openSession();

	void closeSession();

	void updatePing();

}
