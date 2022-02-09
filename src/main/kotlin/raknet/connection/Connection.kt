package raknet.connection

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.nio.NioEventLoopGroup
import raknet.Server
import raknet.message.*
import raknet.message.datagram.Datagram
import raknet.message.protocol.*
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

const val MIN_MTU: Short = 576
const val MAX_MTU: Short = 1492

enum class ConnectionState {
    INITIALIZING, CONNECTING,
    CONNECTED, DISCONNECTED
}

enum class TimeComponent(private val millis: Long) {
    UPDATE(10), TIMEOUT(30_000L);

    fun toLong(): Long = millis
}

class Connection(
    val address: InetSocketAddress,
    val server: Server,
    private val context: ChannelHandlerContext,
    val mtuSize: Short,
    val guid: Long,
) {
    private var connectionState: ConnectionState = ConnectionState.INITIALIZING
    private val internalsHandler = InternalsHandler(this, context)
    private var worker: NioEventLoopGroup = NioEventLoopGroup()

    init {
        worker.scheduleAtFixedRate(this::tick, 0, TimeComponent.UPDATE.toLong(), TimeUnit.MILLISECONDS)
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
                connectionState = ConnectionState.CONNECTING
                internalsHandler.sendInternal(ConnectionRequestAccepted(
                    clientAddress = this.address,
                    systemIndex = 0,
                    requestTime = packet.time,
                    time = server.getUptime()
                ))
            }
            is NewIncomingConnection -> {
                connectionState = ConnectionState.CONNECTED
            }
            is ConnectedPing -> {
                internalsHandler.sendInternal(ConnectedPong(
                    pingTime = packet.time,
                    pongTime = server.getUptime()
                ))
            }
            is UserMessage -> {
                // Do stuff & then release the buffer :)
                packet.buffer.release()
            }
        }
    }

    fun send(packet: OnlineMessage, immediate: Boolean = false) {
        internalsHandler.send(packet, immediate)
    }

    fun close(reason: DisconnectionReason) {
        // TODO: Some type of logging system
        worker.shutdownGracefully()
        context.close()
        connectionState = ConnectionState.DISCONNECTED
    }

}