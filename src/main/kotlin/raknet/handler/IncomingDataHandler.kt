package raknet.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.DatagramPacket
import raknet.Magic
import raknet.packet.DataPacket
import raknet.packet.PacketType
import raknet.packet.protocol.connected.ConnectedPingPacket
import raknet.packet.protocol.connected.ConnectedPongPacket
import raknet.packet.protocol.connected.reply.OpenConnectionReply1Packet
import raknet.packet.protocol.connected.reply.OpenConnectionReply2Packet
import raknet.packet.protocol.connected.request.OpenConnectionRequest1Packet
import raknet.packet.protocol.connected.request.OpenConnectionRequest2Packet
import raknet.packet.protocol.unconnected.UnconnectedPingPacket
import raknet.packet.protocol.unconnected.UnconnectedPongPacket
import java.net.InetSocketAddress

class IncomingDataHandler(private val handler: NetworkHandler): SimpleChannelInboundHandler<DatagramPacket>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket) {
        val sender = msg.sender()
        val buffer = msg.content()
        // Capture buffer data and transform it into a packet if possible
        // We could use a packet factory here, but I'm not sure if that's the best avenue to take at the moment
        val id = buffer.readUnsignedByte()
        val type: PacketType = PacketType.find(id) ?: return
        println("Received packet $type from $sender")
        when(type) {
            PacketType.UNCONNECTED_PING -> {
                val ping = UnconnectedPingPacket.from(buffer)
                val response = UnconnectedPongPacket(
                    pingId = ping.time,
                    magic = Magic,
                    guid = handler.server.guid.mostSignificantBits,
                    serverName = handler.server.identifier.toString(),
                )
                sendPacket(ctx, response, sender)
            }
            PacketType.CONNECTED_PING -> {
                val ping = ConnectedPingPacket.from(buffer)
                val response = ConnectedPongPacket(
                    pingTime = ping.time,
                    pongTime = System.currentTimeMillis(),
                )
                sendPacket(ctx, response, sender)
            }
            PacketType.OPEN_CONNECTION_REQUEST_1 -> {
                val request = OpenConnectionRequest1Packet.from(buffer)
                val response = OpenConnectionReply1Packet(
                    magic = Magic,
                    serverGuid = handler.server.guid.mostSignificantBits,
                    useSecurity = false,
                    mtuSize = request.mtuSize,
                )
                sendPacket(ctx, response, sender)
            }
            PacketType.OPEN_CONNECTION_REQUEST_2 -> {
                val request = OpenConnectionRequest2Packet.from(buffer)
                val response = OpenConnectionReply2Packet(
                    magic = Magic,
                    serverGuid = handler.server.guid.mostSignificantBits,
                    mtuSize = request.mtuSize,
                    clientAddress = sender,
                    encryptionEnabled = false,
                )
                sendPacket(ctx, response, sender)
            }
            else -> throw RuntimeException("Encountered unexpected packet type: $type")
        }
    }

    fun sendPacket(ctx: ChannelHandlerContext, packet: DataPacket, address: InetSocketAddress) {
        // println("Sending packet $packet to address $address")
        ctx.writeAndFlush(DatagramPacket(
            packet.prepare(),
            address
        ))
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
    }
}