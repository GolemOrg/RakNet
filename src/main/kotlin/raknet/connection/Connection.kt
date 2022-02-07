package raknet.connection

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.nio.NioEventLoopGroup
import raknet.Server
import raknet.packet.*
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class Connection(
    val address: InetSocketAddress,
    private val server: Server,
    private val context: ChannelHandlerContext,
    private val mtuSize: Short,
    private val guid: Long,
) {

    private var worker: NioEventLoopGroup = NioEventLoopGroup()

    init {
        worker.scheduleAtFixedRate(this::tick, 0, 10, TimeUnit.MILLISECONDS)
    }

    private fun tick() {}

    private fun handle(packet: DataMessage) {}

    private fun send(packet: OnlineMessage) {}

    fun close(reason: DisconnectionReason) {}

}