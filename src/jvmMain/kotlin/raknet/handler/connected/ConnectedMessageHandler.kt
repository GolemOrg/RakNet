package raknet.handler.connected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import raknet.Server
import raknet.handler.MessageEnvelope
import raknet.message.*
import raknet.message.datagram.Datagram

class ConnectedMessageHandler(private val server: Server): SimpleChannelInboundHandler<MessageEnvelope<OnlineMessage>>() {

    override fun acceptInboundMessage(msg: Any?): Boolean =  msg is MessageEnvelope<*> && msg.content() is OnlineMessage

    override fun channelRead0(ctx: ChannelHandlerContext, msg: MessageEnvelope<OnlineMessage>) {
        val connection = server.getConnection(msg.sender())!!
        when(val message = msg.content()) {
            is Acknowledge -> connection.handleInternal(message)
            is NAcknowledge -> connection.handleInternal(message)
            is Datagram -> connection.handleInternal(message)
        }
    }
}