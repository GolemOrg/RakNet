package raknet.handler.connected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import raknet.Server
import raknet.handler.MessageEnvelope
import raknet.message.*

class ConnectedMessageHandler(private val server: Server): SimpleChannelInboundHandler<MessageEnvelope<OnlineMessage>>() {

    override fun acceptInboundMessage(msg: Any?): Boolean =  msg is MessageEnvelope<*> && msg.content() is OnlineMessage

    override fun channelRead0(ctx: ChannelHandlerContext, msg: MessageEnvelope<OnlineMessage>) {

    }
}