package raknet.connection

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.nio.NioEventLoopGroup
import raknet.Server
import raknet.message.*
import raknet.message.datagram.Datagram
import raknet.message.protocol.ConnectionRequest
import raknet.message.protocol.ConnectionRequestAccepted
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class Connection(
    val address: InetSocketAddress,
    private val server: Server,
    private val context: ChannelHandlerContext,
    private val mtuSize: Short,
    private val guid: Long,
) {
    private val internalsHandler = InternalsHandler(this, context)
    private var worker: NioEventLoopGroup = NioEventLoopGroup()

    init {
        worker.scheduleAtFixedRate(this::tick, 0, 10, TimeUnit.MILLISECONDS)
    }

    private fun tick() {
        internalsHandler.tick()
    }

    fun handleInternal(packet: Acknowledge) = internalsHandler.handle(packet)
    fun handleInternal(packet: NAcknowledge) = internalsHandler.handle(packet)
    fun handleInternal(packet: Datagram) = internalsHandler.handle(packet)

    fun handle(packet: OnlineMessage) {
        when(packet) {
            is ConnectionRequest -> {
                val accepted = ConnectionRequestAccepted(
                    clientAddress = address,
                    systemIndex = 0,
                    requestTime = packet.time,
                    time = server.getUptime()
                )
                send(accepted, true)
            }
        }
    }

    fun send(packet: OnlineMessage, immediate: Boolean = false) {
        internalsHandler.send(packet, immediate)
    }

    fun close(reason: DisconnectionReason) {
        worker.shutdownGracefully()
        context.close()
    }

}