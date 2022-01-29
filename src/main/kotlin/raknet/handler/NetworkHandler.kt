package raknet.handler

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import raknet.Server
import raknet.connection.Connection
import raknet.packet.DataPacket

class NetworkHandler(val server: Server) {
    private val port: Int = server.getPort()
    private val group = NioEventLoopGroup()

    fun start() {
        try {
            val bootstrap = Bootstrap()
            bootstrap
                .channel(NioDatagramChannel::class.java)
                .group(group)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(IncomingDataHandler(this))

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

    fun handleWithoutSession() {

    }

    fun handleWithSession() {

    }

    fun shutdown() {
        group.shutdownGracefully()
    }

    fun receive(): Pair<Connection, DataPacket>? {
        return null
    }
}