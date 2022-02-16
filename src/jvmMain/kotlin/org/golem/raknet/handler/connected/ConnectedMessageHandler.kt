package org.golem.raknet.handler.connected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.golem.raknet.Server
import org.golem.raknet.handler.MessageEnvelope
import org.golem.raknet.message.*
import org.golem.raknet.message.datagram.Datagram

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