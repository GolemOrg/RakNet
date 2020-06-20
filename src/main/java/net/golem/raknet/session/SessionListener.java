package net.golem.raknet.session;

import net.golem.raknet.protocol.DataPacket;

public interface SessionListener {

	void handlePacket(DataPacket packet);

}
