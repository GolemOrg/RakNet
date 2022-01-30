package raknet.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.DatagramPacket
import raknet.Magic
import raknet.Server
import raknet.connection.Connection
import raknet.packet.DataPacket
import raknet.packet.PacketType
import raknet.packet.protocol.ConnectedPing
import raknet.packet.protocol.ConnectedPong
import raknet.packet.protocol.OpenConnectionReply1
import raknet.packet.protocol.OpenConnectionReply2
import raknet.packet.protocol.OpenConnectionRequest1
import raknet.packet.protocol.OpenConnectionRequest2
import raknet.packet.protocol.UnconnectedPing
import raknet.packet.protocol.UnconnectedPong
import java.net.InetSocketAddress

class IncomingDataHandler(private val server: Server): SimpleChannelInboundHandler<DatagramPacket>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket) {
        val sender = msg.sender()
        val buffer = msg.content()
        // Capture buffer data and transform it into a packet if possible
        // We could use a packet factory here, but I'm not sure if that's the best avenue to take at the moment
        val id = buffer.readUnsignedByte()
        val type: PacketType? = PacketType.find(id)
        if(type == null) {
            println("Received unknown packet of type $id from $sender")
            return
        }
        if(!server.hasConnection(sender)) {
            val response = handleUnconnected(ctx, type, buffer, sender)
            if (response == null) {
                println("Received unconnected packet of type $id from $sender")
                return
            }
            ctx.sendPacket(sender, response)
        } else {
            val connection = server.getConnection(sender)

        }
    }

    private fun handleUnconnected(ctx: ChannelHandlerContext, type: PacketType, buffer: ByteBuf, sender: InetSocketAddress): DataPacket? {
        return when(type) {
            PacketType.UNCONNECTED_PING -> {
                val ping = UnconnectedPing.from(buffer)
                UnconnectedPong(
                    pingId = ping.time,
                    magic = Magic,
                    guid = server.guid.mostSignificantBits,
                    serverName = server.identifier.toString(),
                )
            }
            PacketType.CONNECTED_PING -> {
                val ping = ConnectedPing.from(buffer)
                println("Latency is " + (System.currentTimeMillis() - ping.time) + "ms")
                ConnectedPong(
                    pingTime = ping.time,
                    pongTime = System.currentTimeMillis(),
                )
            }
            PacketType.OPEN_CONNECTION_REQUEST_1 -> {
                val request = OpenConnectionRequest1.from(buffer)
                println("Received first request with details: $request")
                OpenConnectionReply1(
                    magic = Magic,
                    serverGuid = server.guid.mostSignificantBits,
                    useSecurity = false,
                    mtuSize = request.mtuSize,
                )
            }
            PacketType.OPEN_CONNECTION_REQUEST_2 -> {
                val request = OpenConnectionRequest2.from(buffer)
                println("Received seconds request with details: $request")
                server.addConnection(Connection(
                    context = ctx,
                    address = sender,
                    mtuSize = request.mtuSize
                ))
                OpenConnectionReply2(
                    magic = Magic,
                    serverGuid = server.guid.mostSignificantBits,
                    mtuSize = request.mtuSize,
                    clientAddress = sender,
                    encryptionEnabled = false,
                )
            }
            else -> null
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
    }

    private fun ChannelHandlerContext.sendPacket(address: InetSocketAddress, packet: DataPacket): ChannelFuture = writeAndFlush(DatagramPacket(packet.prepare(), address))
}