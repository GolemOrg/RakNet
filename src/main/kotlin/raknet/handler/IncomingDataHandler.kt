package raknet.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.DatagramPacket
import raknet.packet.PacketType
import raknet.packet.protocol.UnconnectedPingPacket
import raknet.packet.protocol.UnconnectedPongPacket

class IncomingDataHandler constructor(private val handler: NetworkHandler): SimpleChannelInboundHandler<DatagramPacket>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket) {
        val connection = msg.sender()
        val buffer = msg.content()
        // Capture buffer data and transform it into a packet if possible
        // We could use a packet factory here, but I'm not sure if that's the best avenue to take at the moment
        val id = buffer.readUnsignedByte()
        val type: PacketType = PacketType.find(id) ?: return

        buffer.retain()
        when(type) {
            PacketType.UNCONNECTED_PING -> {
                val ping = UnconnectedPingPacket.from(buffer)
                handler.server.log("Received packet $ping from address ${msg.sender()}")
                val response = UnconnectedPongPacket(
                    pingId = ping.time,
                    guid = handler.server.guid.mostSignificantBits,
                    serverName = handler.server.identifier.toString(),
                )
                ctx.write(DatagramPacket(response.prepare(), connection))
                handler.server.log("Sent pong back to address ${msg.sender()}")
            }
            else -> {}
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        ctx?.flush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
    }
}