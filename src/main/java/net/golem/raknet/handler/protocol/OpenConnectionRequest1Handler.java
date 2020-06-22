package net.golem.raknet.handler.protocol;

import io.netty.channel.ChannelHandlerContext;
import net.golem.raknet.RakNetServer;
import net.golem.raknet.handler.RakNetInboundPacketHandler;
import net.golem.raknet.handler.RakNetPacketEnvelope;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.connected.reply.OpenConnectionReply1Packet;
import net.golem.raknet.protocol.connected.request.OpenConnectionRequest1Packet;

import java.net.SocketAddress;

public class OpenConnectionRequest1Handler extends RakNetInboundPacketHandler<OpenConnectionRequest1Packet> {

	public OpenConnectionRequest1Handler(RakNetServer server) {
		super(server, OpenConnectionRequest1Packet.class);
	}

	@Override
	public DataPacket handlePacket(ChannelHandlerContext context, RakNetPacketEnvelope<OpenConnectionRequest1Packet, SocketAddress> message) {
		OpenConnectionRequest1Packet packet = message.content();
		OpenConnectionReply1Packet pk = new OpenConnectionReply1Packet();
		pk.guid = server.getGlobalUniqueId().getMostSignificantBits();
		pk.maximumTransferUnits = packet.maximumTransferUnits;
		return pk;
	}
}
