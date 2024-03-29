package org.golem.raknet.handler.unconnected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.golem.raknet.CURRENT_PROTOCOL_VERSION
import org.golem.raknet.types.Magic
import org.golem.raknet.Server
import org.golem.raknet.connection.Connection
import org.golem.raknet.handler.MessageEnvelope
import org.golem.raknet.message.OfflineMessage
import org.golem.raknet.message.protocol.*

class UnconnectedMessageHandler(private val server: Server): SimpleChannelInboundHandler<MessageEnvelope<OfflineMessage>>() {

    override fun acceptInboundMessage(msg: Any?): Boolean =  msg is MessageEnvelope<*> && msg.content() is OfflineMessage

    override fun channelRead0(ctx: ChannelHandlerContext, msg: MessageEnvelope<OfflineMessage>) {
        val response: OfflineMessage = when(val packet = msg.content()) {
            is UnconnectedPing -> {
                if(server.hasConnection(msg.sender())) {
                    return
                }
                UnconnectedPong(
                    pingId = packet.time,
                    magic = Magic,
                    guid = server.guid.mostSignificantBits,
                    // TODO: Server name
                    serverName = server.name,
                )
            }
            is OpenConnectionRequest1 -> {
                if(packet.protocolVersion != CURRENT_PROTOCOL_VERSION) {
                    IncompatibleProtocol(
                        protocol = CURRENT_PROTOCOL_VERSION,
                        magic = Magic,
                        serverGuid = server.guid.mostSignificantBits,
                    )
                } else {
                    OpenConnectionReply1(
                        magic = Magic,
                        serverGuid = server.guid.mostSignificantBits,
                        useSecurity = false,
                        mtuSize = (packet.mtuSize + 28).toShort(),
                    )
                }
            }
            is OpenConnectionRequest2 -> {
                var mtuSize = packet.mtuSize
                if(mtuSize < Connection.MIN_MTU) {
                    // Do not attempt to respond if the MTU is this small
                    return
                }
                // MTU size will never exceed `Connection.MAX_MTU`
                mtuSize = minOf(mtuSize, Connection.MAX_MTU)
                server.addConnection(Connection(
                    address = msg.sender(),
                    server = server,
                    context = ctx,
                    mtuSize = mtuSize,
                    guid = packet.clientGuid
                ))
                OpenConnectionReply2(
                    magic = Magic,
                    serverGuid = server.guid.mostSignificantBits,
                    mtuSize = mtuSize,
                    clientAddress = packet.serverAddress,
                    encryptionEnabled = false,
                )
            }
            else -> throw IllegalArgumentException("Unsupported packet: $packet")
        }
        ctx.writeAndFlush(MessageEnvelope(response, msg.sender()))
    }

}