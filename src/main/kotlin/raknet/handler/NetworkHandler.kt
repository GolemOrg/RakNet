package raknet.handler

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import raknet.Server
import raknet.connection.Connection
import raknet.packet.DataPacket
import raknet.packet.PacketType
import java.net.Inet4Address
import java.net.InetSocketAddress

class NetworkHandler(private val server: Server) : SimpleChannelInboundHandler<DatagramPacket>() {
    private val port: Int = server.getPort()
    private val group = NioEventLoopGroup()

    fun start() {
        try {
            val bootstrap = Bootstrap()
            bootstrap
                .channel(NioDatagramChannel::class.java)
                .group(group)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(this)

            server.log("Created channel handlers")

            bootstrap
                .bind(port)
                .channel()
                .closeFuture()
                .sync()
        } catch (e: Exception) {
            // Catch errors here
            e.printStackTrace()
            server.shutdown()
        } finally {
            group.shutdownGracefully()
        }
    }

    fun shutdown() {
        group.shutdownGracefully()
    }

    fun receive(): Pair<Connection, DataPacket>? {
        return null
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket) {
        val connection = msg.sender()
        val buffer = msg.content()
        // Capture buffer data and transform it into a packet if possible
        // We could use a packet factory here, but I'm not sure if that's the best avenue to take at the moment
        val id = buffer.readUnsignedByte()
        val type: PacketType? = PacketType.find(id)
        if(type == null) {
            server.log("Received unknown packet type %02x from %s".format(id, connection.address))
            return
        }
        server.log("Received %s packet from %s".format(type, connection))
    }

}