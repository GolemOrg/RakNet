package net.golem.raknet.session;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.RakNetServer;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.HashMap;

@Log4j2
public class SessionManager {

	private RakNetServer server;

	private HashMap<InetSocketAddress, RakNetSession> sessions = new HashMap<>();

	private Class<? extends RakNetSession> sessionInterface = RakNetSession.class;

	public SessionManager(RakNetServer server) {
		this.server = server;
	}

	public RakNetServer getServer() {
		return server;
	}

	public HashMap<InetSocketAddress, RakNetSession> getSessions() {
		return sessions;
	}

	public Class<? extends RakNetSession> getSessionInterface() {
		return sessionInterface;
	}

	public void setSessionInterface(Class<? extends RakNetSession> sessionInterface) {
		this.sessionInterface = sessionInterface;
	}

	public RakNetSession get(InetSocketAddress address) {
		return sessions.getOrDefault(address, null);
	}

	private void add(RakNetSession session) {
		sessions.put(session.getAddress(), session);
	}

	public void remove(RakNetSession session) {
		sessions.remove(session.getAddress());
	}

	public boolean contains(InetSocketAddress address) {
		return sessions.containsKey(address);
	}

	public RakNetSession create(InetSocketAddress address) {
		if(contains(address)) {
			return get(address);
		}
		RakNetSession session = null;
		try {
			session = sessionInterface.getConstructor(RakNetServer.class, SessionManager.class, ChannelHandlerContext.class, InetSocketAddress.class).newInstance(server, this, server.getContext(), address);
			add(session);
			if(server.verbose) log.info("Created session [{}]", address);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return session;
	}
}
