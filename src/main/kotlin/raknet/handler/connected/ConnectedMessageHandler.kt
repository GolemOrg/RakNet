package raknet.handler.connected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import raknet.Server
import raknet.handler.PacketEnvelope
import raknet.packet.*

class ConnectedMessageHandler(private val server: Server): SimpleChannelInboundHandler<PacketEnvelope<ConnectedPacket>>() {

    override fun acceptInboundMessage(msg: Any?): Boolean =  msg is PacketEnvelope<*> && msg.content() is ConnectedPacket

    override fun channelRead0(ctx: ChannelHandlerContext, msg: PacketEnvelope<ConnectedPacket>) {

    }
}