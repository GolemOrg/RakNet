package raknet.connection

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.nio.NioEventLoopGroup
import raknet.packet.DataPacket
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

enum class TimePeriod(val period: Long) { UPDATE(10), ACK(50), PING(2_000L), STALE(5_000L), TIMEOUT(30_000L) }
enum class State { INITIALIZING, CONNECTING, CONNECTED, DISCONNECTED }

class Connection(
    val address: InetSocketAddress,
    private val context: ChannelHandlerContext,
    private val mtuSize: Int,
) {

    private var state: State = State.INITIALIZING
    // TODO: Listeners
    private var worker: NioEventLoopGroup = NioEventLoopGroup()

    init {
        worker.scheduleAtFixedRate(this::tick, 0, TimePeriod.UPDATE.period, TimeUnit.MILLISECONDS)
    }


    private fun tick() {

    }

    private fun handle(packet: DataPacket) {

    }

    private fun close() {
        try {
            context.close()
            state = State.DISCONNECTED
        } catch(e: Exception) {
            e.printStackTrace()
        } finally {
            worker.shutdownGracefully()
        }
    }

}

