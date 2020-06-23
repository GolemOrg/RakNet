package net.golem.raknet.session;

import net.golem.raknet.protocol.DataPacket;

public interface SessionListener {

	void onPacket(DataPacket packet);

	void onOpen();

	void onClose();


}
