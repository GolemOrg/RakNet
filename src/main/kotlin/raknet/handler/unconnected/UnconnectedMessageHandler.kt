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
        val packet = msg.content()
        val response: UnconnectedPacket = when(packet) {
            is UnconnectedPing -> {
                server.listeners.forEach { it.handleUnconnectedPing(msg.sender(), packet) }
                UnconnectedPong(
                    pingId = packet.time,
                    magic = Magic,
                    guid = server.guid.mostSignificantBits,
                    serverName = server.identifier.toString(),
                )
            }
            is OpenConnectionRequest1 -> {
                server.listeners.forEach { it.handleOpenConnectionRequest1(msg.sender(), packet) }
                OpenConnectionReply1(
                    magic = Magic,
                    serverGuid = server.guid.mostSignificantBits,
                    useSecurity = false,
                    mtuSize = (packet.mtuSize + 28).toShort(),
                )
            }
            is OpenConnectionRequest2 -> {
                server.listeners.forEach { it.handleOpenConnectionRequest2(msg.sender(), packet) }
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