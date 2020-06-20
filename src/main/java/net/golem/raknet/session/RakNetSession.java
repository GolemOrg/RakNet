package net.golem.raknet.session;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.RakNetServer;
import net.golem.raknet.enums.PacketReliability;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.session.codec.PacketDecodeLayer;
import net.golem.raknet.session.codec.PacketEncodeLayer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Log4j2
public class RakNetSession {

	/**
	 * TimeUnits, in milliseconds
	 */
	enum TimeUnits {
		UPDATE(10),
		STALE(1000 * 5),
		TIMEOUT(1000 * 30);

		private int length;

		TimeUnits(int length) {
			this.length = length;
		}

		public int getLength() {
			return length;
		}
	}

	private RakNetServer server;
	private SessionManager sessionManager;

	private ChannelHandlerContext context;

	private InetSocketAddress address;

	private SessionState state;

	private NioEventLoopGroup worker = new NioEventLoopGroup();

	private ArrayList<SessionListener> listeners = new ArrayList<>();

	private long lastReceivedTime = System.currentTimeMillis();

	private PacketDecodeLayer decodeLayer = new PacketDecodeLayer(this);
	private PacketEncodeLayer encodeLayer = new PacketEncodeLayer(this);

	private boolean active = true;
	private boolean closed = false;

	private int maximumTransferUnits = -1;

	public RakNetSession(RakNetServer server, SessionManager sessionManager, ChannelHandlerContext context, InetSocketAddress address) {
		this.server = server;
		this.sessionManager = sessionManager;
		this.context = context;
		this.address = address;
		worker.scheduleAtFixedRate(this::tick, 0, TimeUnits.UPDATE.getLength(), TimeUnit.MILLISECONDS);
	}

	public RakNetServer getServer() {
		return server;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public ChannelHandlerContext getContext() {
		return context;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public SessionState getState() {
		return state;
	}

	public void setState(SessionState state) {
		this.state = state;
	}

	public PacketDecodeLayer getDecodeLayer() {
		return decodeLayer;
	}

	public PacketEncodeLayer getEncodeLayer() {
		return encodeLayer;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public int getMaximumTransferUnits() {
		return maximumTransferUnits;
	}

	public void setMaximumTransferUnits(int maximumTransferUnits) {
		this.maximumTransferUnits = maximumTransferUnits;
	}

	public void addListener(SessionListener listener) {
		if(listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}

	public void handleListeners(DataPacket packet) {
		listeners.forEach(listener -> listener.handlePacket(packet));
	}

	public void sendPacket(DataPacket packet, PacketReliability reliability, boolean immediate) {
		if(this.isClosed()) {
			return;
		}
		this.getContext().writeAndFlush(new DatagramPacket(packet.create(), this.getAddress()));
	}

	public void sendPacket(DataPacket packet, boolean immediate) {
		sendPacket(packet, PacketReliability.UNRELIABLE, immediate);
	}

	public void sendPacket(DataPacket packet) {
		sendPacket(packet, false);
	}

	public void handle(DataPacket packet) {

	}

	protected void tick() {
		if(isClosed()) {
			return;
		}
		long currentTime = System.currentTimeMillis();
		update(currentTime);
	}

	public void updateReceivedTime() {
		lastReceivedTime = System.currentTimeMillis();
	}

	public void update(long currentTime) {
		long difference = currentTime - lastReceivedTime;

		if(difference > TimeUnits.STALE.getLength() && isActive()) {
			setActive(false);
			if(sessionManager.getServer().verbose) log.info("Stale session: [{}]", this::getAddress);
		}

		if(difference > TimeUnits.TIMEOUT.getLength()) {
			close("timeout");
			return;
		}

		decodeLayer.update(currentTime);
		encodeLayer.update(currentTime);
	}

	public void close(String reason) {
		if(isClosed()) {
			if(sessionManager.getServer().verbose) log.info("Attempted to close session [{}] after it had been closed", getAddress());
			return;
		}
		if(sessionManager.getServer().verbose) log.info("Closing session [{}] due to {}", getAddress(), reason);
		sessionManager.remove(this);
		decodeLayer.close();
		encodeLayer.close();
		worker.shutdownGracefully();
		setClosed(true);
	}

	public void close() {
		close("Unknown");
	}

}
