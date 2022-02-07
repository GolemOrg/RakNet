package raknet.handler.unconnected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import raknet.CURRENT_PROTOCOL_VERSION
import raknet.types.Magic
import raknet.Server
import raknet.connection.Connection
import raknet.handler.MessageEnvelope
import raknet.message.OfflineMessage
import raknet.message.protocol.*

class UnconnectedMessageHandler(private val server: Server): SimpleChannelInboundHandler<MessageEnvelope<OfflineMessage>>() {

    override fun acceptInboundMessage(msg: Any?): Boolean =  msg is MessageEnvelope<*> && msg.content() is OfflineMessage

    override fun channelRead0(ctx: ChannelHandlerContext, msg: MessageEnvelope<OfflineMessage>) {
        val response: OfflineMessage = when(val packet = msg.content()) {
            is UnconnectedPing -> {
                UnconnectedPong(
                    pingId = packet.time,
                    magic = Magic,
                    guid = server.guid.mostSignificantBits,
                    // TODO: Server name
                    serverName = "MCPE;Golem Server;475;1.18.0;0;100;${server.guid.mostSignificantBits};Golem;Creative;1;19132;19132"
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
                server.addConnection(Connection(
                    address = msg.sender(),
                    server = server,
                    context = ctx,
                    mtuSize = packet.mtuSize,
                    guid = packet.clientGuid
                ))

                OpenConnectionReply2(
                    magic = Magic,
                    serverGuid = server.guid.mostSignificantBits,
                    mtuSize = packet.mtuSize,
                    clientAddress = packet.serverAddress,
                    encryptionEnabled = false,
                )
            }
            else -> throw IllegalArgumentException("Unsupported packet: $packet")
        }
        ctx.writeAndFlush(MessageEnvelope(response, msg.sender()))
    }

}