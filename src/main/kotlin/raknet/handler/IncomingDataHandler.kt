package raknet.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.DatagramPacket
import raknet.Magic
import raknet.packet.DataPacket
import raknet.packet.PacketType
import raknet.packet.protocol.UnconnectedPingPacket
import raknet.packet.protocol.UnconnectedPongPacket
import java.net.InetSocketAddress

class IncomingDataHandler constructor(private val handler: NetworkHandler): SimpleChannelInboundHandler<DatagramPacket>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket) {
        val sender = msg.sender()
        val buffer = msg.content()
        // Capture buffer data and transform it into a packet if possible
        // We could use a packet factory here, but I'm not sure if that's the best avenue to take at the moment
        val id = buffer.readUnsignedByte()
        val type: PacketType = PacketType.find(id) ?: return

        buffer.retain()
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

            }
            PacketType.OPEN_CONNECTION_REQUEST_1 -> {

            }
            PacketType.OPEN_CONNECTION_REQUEST_2 -> {

            }
            else -> throw RuntimeException("Encountered unexpected packet type: $type")
        }
    }

    fun sendPacket(ctx: ChannelHandlerContext, packet: DataPacket, address: InetSocketAddress) {
        println("Sending packet $packet to address $address")
        ctx.writeAndFlush(DatagramPacket(
            packet.prepare(),
            address
        ))
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
    }
}