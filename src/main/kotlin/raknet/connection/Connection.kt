package raknet.connection

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.nio.NioEventLoopGroup
import raknet.Server
import raknet.message.*
import raknet.message.datagram.Datagram
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class Connection(
    val address: InetSocketAddress,
    private val server: Server,
    private val context: ChannelHandlerContext,
    private val mtuSize: Short,
    private val guid: Long,
) {
    private val internalsHandler = InternalsHandler(this)
    private var worker: NioEventLoopGroup = NioEventLoopGroup()

    init {
        worker.scheduleAtFixedRate(this::tick, 0, 10, TimeUnit.MILLISECONDS)
    }

    private fun tick() {}

    fun handleInternal(packet: Acknowledge) = internalsHandler.handle(packet)
    fun handleInternal(packet: NAcknowledge) = internalsHandler.handle(packet)
    fun handleInternal(packet: Datagram) = internalsHandler.handle(packet)

    private fun handle(packet: OnlineMessage) {}

    private fun send(packet: OnlineMessage) {}

    fun close(reason: DisconnectionReason) {
        worker.shutdownGracefully()
        context.close()
    }

}