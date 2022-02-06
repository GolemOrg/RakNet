package raknet.handler.unconnected

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import raknet.Magic
import raknet.Server
import raknet.connection.Connection
import raknet.handler.PacketEnvelope
import raknet.packet.UnconnectedPacket
import raknet.packet.protocol.*

class UnconnectedMessageHandler(private val server: Server): SimpleChannelInboundHandler<PacketEnvelope<UnconnectedPacket>>() {

    override fun acceptInboundMessage(msg: Any?): Boolean =  msg is PacketEnvelope<*> && msg.content() is UnconnectedPacket

    override fun channelRead0(ctx: ChannelHandlerContext, msg: PacketEnvelope<UnconnectedPacket>) {
        val response: UnconnectedPacket = when(val packet = msg.content()) {
            is UnconnectedPing -> {
                UnconnectedPong(
                    pingId = packet.time,
                    magic = Magic,
                    guid = server.guid.mostSignificantBits,
                    serverName = "" // TODO: Server name
                )
            }
            is OpenConnectionRequest1 -> {
                OpenConnectionReply1(
                    magic = Magic,
                    serverGuid = server.guid.mostSignificantBits,
                    useSecurity = false,
                    mtuSize = (packet.mtuSize + 28).toShort(),
                )
            }
            is OpenConnectionRequest2 -> {
                val response = OpenConnectionReply2(
                    magic = Magic,
                    serverGuid = server.guid.mostSignificantBits,
                    mtuSize = packet.mtuSize,
                    clientAddress = packet.serverAddress,
                    encryptionEnabled = false,
                )

                server.addConnection(Connection(
                    address = msg.sender(),
                    server = server,
                    context = ctx,
                    mtuSize = response.mtuSize,
                    guid = packet.clientGuid
                ))
                response
            }
            else -> throw IllegalArgumentException("Unsupported packet: $packet")
        }
        ctx.writeAndFlush(PacketEnvelope(response, msg.sender()))
    }

}