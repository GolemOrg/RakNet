package net.golem.raknet.handler;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import net.golem.raknet.RakNetServer;
import net.golem.raknet.codec.PacketEncoder;
import net.golem.raknet.protocol.DataPacket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class RakNetInboundPacketHandler<I extends DataPacket> extends SimpleChannelInboundHandler<RakNetPacketEnvelope<I, SocketAddress>> {

	protected RakNetServer server;

	private Class<? extends DataPacket> packetClass;

	public RakNetInboundPacketHandler(RakNetServer server, Class<? extends DataPacket> packetClass) {
		this.server = server;
		this.packetClass = packetClass;
	}

	@Override
	public boolean acceptInboundMessage(Object msg) {
		try {
			if(super.acceptInboundMessage(msg) && msg instanceof RakNetPacketEnvelope) {
				//noinspection rawtypes
				return this.packetClass.isAssignableFrom(((RakNetPacketEnvelope) msg).content().getClass());
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return false;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext context, RakNetPacketEnvelope<I, SocketAddress> message) {
		DataPacket packet = handlePacket(context, message);
		if(packet != null) {
			sendPacket(context, packet, message.recipient());
		}
		message.release();
	}

	public abstract DataPacket handlePacket(ChannelHandlerContext context, RakNetPacketEnvelope<I, SocketAddress> message);

	public void sendPacket(ChannelHandlerContext context, DataPacket packet, InetSocketAddress recipient) {
		PacketEncoder encoder = new PacketEncoder();
		context.writeAndFlush(new DatagramPacket(packet.write(encoder), recipient));
	}

}
