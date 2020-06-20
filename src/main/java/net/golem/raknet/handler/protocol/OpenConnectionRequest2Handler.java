package net.golem.raknet.handler.protocol;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.RakNetServer;
import net.golem.raknet.handler.RakNetInboundPacketHandler;
import net.golem.raknet.handler.RakNetPacketEnvelope;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.connected.reply.OpenConnectionReply2Packet;
import net.golem.raknet.protocol.connected.request.OpenConnectionRequest2Packet;
import net.golem.raknet.session.RakNetSession;

@Log4j2
public class OpenConnectionRequest2Handler extends RakNetInboundPacketHandler<OpenConnectionRequest2Packet> {

	public OpenConnectionRequest2Handler(RakNetServer server) {
		super(server, OpenConnectionRequest2Packet.class);
	}

	@Override
	public DataPacket handlePacket(ChannelHandlerContext context, RakNetPacketEnvelope<OpenConnectionRequest2Packet> message) {
		OpenConnectionRequest2Packet packet = message.content();

		OpenConnectionReply2Packet pk = new OpenConnectionReply2Packet();
		pk.serverGuid = server.getGlobalUniqueId().getMostSignificantBits();
		pk.clientAddress = message.recipient();
		pk.maximumTransferUnits = packet.maximumTransferUnits;

		RakNetSession session = server.getSessionManager().create(message.recipient());
		session.setMaximumTransferUnits(packet.maximumTransferUnits);

		return pk;
	}
}
