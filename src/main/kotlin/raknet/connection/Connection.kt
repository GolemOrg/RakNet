package raknet.connection

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.nio.NioEventLoopGroup
import raknet.Server
import raknet.enums.Reliability
import raknet.handler.PacketEnvelope
import raknet.packet.*
import raknet.packet.protocol.*
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

enum class TimePeriod(val period: Long) {
    UPDATE(15), PING(5_000L), TIMEOUT(30_000L);

    fun check(time: Long): Boolean = System.currentTimeMillis() - time > period
}
enum class State { UNCONNECTED, CONNECTING, CONNECTED, DISCONNECTED }
enum class Priority { LOW, MEDIUM, HIGH, IMMEDIATE }

class Connection(
    val address: InetSocketAddress,
    private val server: Server,
    private val context: ChannelHandlerContext,
    private val mtuSize: Short,
    private val guid: Long,
) {

    private var worker: NioEventLoopGroup = NioEventLoopGroup()

    init {
        worker.scheduleAtFixedRate(this::tick, 0, TimePeriod.UPDATE.period, TimeUnit.MILLISECONDS)
    }

    private fun tick() {

    }

    private fun handle(packet: DataPacket) {

    }

    private fun send(packet: ConnectedPacket, priority: Priority = Priority.MEDIUM) {

    }

    fun close(reason: String) {

    }

}