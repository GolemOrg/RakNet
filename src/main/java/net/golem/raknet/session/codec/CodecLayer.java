package net.golem.raknet.session.codec;

import net.golem.raknet.session.RakNetSession;

public abstract class CodecLayer {

	protected RakNetSession session;

	public CodecLayer(RakNetSession session) {
		this.session = session;
	}

	public RakNetSession getSession() {
		return session;
	}

	public abstract void update(long currentTime);

	public abstract void close();
}
