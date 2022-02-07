package raknet

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.util.ResourceLeakDetector
import raknet.connection.Connection
import raknet.connection.DisconnectionReason
import raknet.handler.MessageEncoder
import raknet.handler.connected.ConnectedMessageDecoder
import raknet.handler.connected.ConnectedMessageHandler
import raknet.handler.unconnected.UnconnectedMessageDecoder
import raknet.handler.unconnected.UnconnectedMessageHandler
import raknet.packet.OnlineMessage
import raknet.packet.OfflineMessage
import java.net.InetSocketAddress
import java.util.*
import kotlin.collections.HashMap

class Server(
    val port: Int = 19132,
    val guid: UUID = UUID.randomUUID()
) {
    init { ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID) }

    private val group = NioEventLoopGroup()
    private val startTime: Long = System.currentTimeMillis()
    private val connections: HashMap<InetSocketAddress, Connection> = HashMap()

    fun start() {
        try {
            val bootstrap = Bootstrap()
                .channel(NioDatagramChannel::class.java)
                .group(group)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(object: ChannelInitializer<NioDatagramChannel>() {
                    override fun initChannel(channel: NioDatagramChannel) {
                        channel.pipeline().addLast(
                            // Decoders
                            UnconnectedMessageDecoder(),
                            ConnectedMessageDecoder(this@Server),
                            // Encoders
                            MessageEncoder<OfflineMessage>(),
                            MessageEncoder<OnlineMessage>(),
                            // Handlers
                            UnconnectedMessageHandler(this@Server),
                            ConnectedMessageHandler(this@Server),
                        )
                    }
                })
            // Bind the server to the port.
            val future = bootstrap.bind(port).sync()
            // Keep the server alive until the socket is closed.
            future.channel().closeFuture().sync()
        } finally {
            group.shutdownGracefully()
        }
    }

    fun shutdown() {
        connections.values.forEach { it.close(DisconnectionReason.ServerClosed) }
        group.shutdownGracefully()
    }


    fun getUptime() = System.currentTimeMillis() - startTime

    fun addConnection(connection: Connection) {
        connections[connection.address] = connection
    }

    fun getConnection(address: InetSocketAddress): Connection? = connections[address]

    fun hasConnection(address: InetSocketAddress): Boolean = connections.containsKey(address)

    fun getConnections(): List<Connection> = connections.values.toList()

    fun closeConnection(address: InetSocketAddress) = connections.remove(address)
    fun closeConnection(connection: Connection) = connections.remove(connection.address)

}