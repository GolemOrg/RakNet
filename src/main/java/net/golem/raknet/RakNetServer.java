package net.golem.raknet;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.handler.codec.PacketDecodeHandler;
import net.golem.raknet.handler.protocol.OpenConnectionRequest1Handler;
import net.golem.raknet.handler.protocol.OpenConnectionRequest2Handler;
import net.golem.raknet.handler.protocol.UnconnectedPingHandler;
import net.golem.raknet.session.SessionManager;

import java.net.InetSocketAddress;
import java.util.UUID;

@Log4j2
public class RakNetServer {

	public NioEventLoopGroup serverGroup;

	private ChannelHandlerContext context;

	private InetSocketAddress localAddress;

	private Identifier identifier;

	private long startTime = System.currentTimeMillis();

	public boolean verbose;

	private UUID guid = UUID.randomUUID();

	private SessionManager sessionManager;


	public RakNetServer(String host, int port, Identifier identifier) {
		this(host, port, identifier, false);
	}

	public RakNetServer(String host, int port, Identifier identifier, boolean verbose) {
		this.localAddress = new InetSocketAddress(host, port);
		this.identifier = identifier;
		this.verbose = verbose;
		this.sessionManager = new SessionManager(this);
		this.create();
	}

	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public UUID getGlobalUniqueId() {
		return guid;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public boolean needsContext() {
		return context == null;
	}

	public ChannelHandlerContext getContext() {
		return context;
	}

	public void setContext(ChannelHandlerContext context) {
		this.context = context;
	}

	public void create() {
		serverGroup = new NioEventLoopGroup();
		new Bootstrap()
				.channel(NioDatagramChannel.class)
				.group(serverGroup)
				.option(ChannelOption.SO_REUSEADDR, true)
				.handler(new ChannelInitializer<NioDatagramChannel>() {
					@Override
					protected void initChannel(NioDatagramChannel channel) {
						channel.pipeline().addLast(
								new PacketDecodeHandler(RakNetServer.this),
								new UnconnectedPingHandler(RakNetServer.this),
								new OpenConnectionRequest1Handler(RakNetServer.this),
								new OpenConnectionRequest2Handler(RakNetServer.this)
						);
						if(verbose) log.info("Created channel handlers");
					}
				})
				.bind(getLocalAddress().getAddress(), getLocalAddress().getPort())
				.syncUninterruptibly();
		if(verbose) log.info("Started server successfully!");
	}

	public long getRakNetTimeMS() {
		return System.currentTimeMillis() - this.startTime;
	}

	public void shutdown() {
		serverGroup.shutdownGracefully();
	}
}
