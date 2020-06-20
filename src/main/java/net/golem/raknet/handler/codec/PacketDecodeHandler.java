package net.golem.raknet.handler.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.log4j.Log4j2;
import net.golem.raknet.RakNetServer;
import net.golem.raknet.handler.RakNetPacketEnvelope;
import net.golem.raknet.protocol.DataPacket;
import net.golem.raknet.protocol.RakNetPacketFactory;
import net.golem.raknet.session.RakNetSession;

import java.util.List;

@Log4j2
public class PacketDecodeHandler extends MessageToMessageDecoder<DatagramPacket> {

	private RakNetServer server;

	public PacketDecodeHandler(RakNetServer server) {
		this.server = server;
	}

	@Override
	protected void decode(ChannelHandlerContext context, DatagramPacket incoming, List<Object> pipeline) {
		if(server.needsContext()) server.setContext(context);
		DataPacket packet = RakNetPacketFactory.from(incoming.content());

		if(server.getSessionManager().contains(incoming.sender())) {
			RakNetSession session = server.getSessionManager().get(incoming.sender());
			session.getDecodeLayer().handleIncoming(packet);
			return;
		}

		pipeline.add(new RakNetPacketEnvelope<>(packet, incoming.sender()));
	}
}
