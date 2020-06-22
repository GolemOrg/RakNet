package net.golem.raknet.handler.protocol;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.RakNetServer;
import net.golem.raknet.handler.RakNetInboundPacketHandler;
import net.golem.raknet.handler.RakNetPacketEnvelope;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.unconnected.UnconnectedPingPacket;
import net.golem.raknet.protocol.unconnected.UnconnectedPongPacket;

import java.net.SocketAddress;

@Log4j2
public class UnconnectedPingHandler extends RakNetInboundPacketHandler<UnconnectedPingPacket> {

	public UnconnectedPingHandler(RakNetServer server) {
		super(server, UnconnectedPingPacket.class);
	}

	@Override
	public DataPacket handlePacket(ChannelHandlerContext context, RakNetPacketEnvelope<UnconnectedPingPacket, SocketAddress> message) {
		UnconnectedPingPacket packet = message.content();

		UnconnectedPongPacket pk = new UnconnectedPongPacket();
		pk.guid = server.getGlobalUniqueId().getMostSignificantBits();
		pk.pingId = packet.pingId;
		pk.serverName = server.getIdentifier().build();

		return pk;
	}
}
