package net.golem.raknet.session.codec;

public interface CodecLayer {

	void update(long currentTime);

	void close();
}
