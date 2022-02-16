package org.golem.raknet.connection

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.nio.NioEventLoopGroup
import org.golem.events.EventBus
import org.golem.raknet.Server
import org.golem.raknet.message.*
import org.golem.raknet.message.datagram.Datagram
import org.golem.raknet.message.protocol.*
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

const val MIN_MTU: Short = 576
const val MAX_MTU: Short = 1492

enum class ConnectionState { INITIALIZING, CONNECTING, CONNECTED, DISCONNECTED }

enum class TimeComponent(private val millis: Long) {
    UPDATE(10),
    PING(5_000),
    TIMEOUT(30_000);

    fun toLong(): Long = millis
}

class Connection(
    val address: InetSocketAddress,
    val server: Server,
    context: ChannelHandlerContext,
    val mtuSize: Short,
    val guid: Long,
) {
    var state: ConnectionState = ConnectionState.INITIALIZING

    private val internalsHandler = InternalsHandler(this, context)
    private var worker: NioEventLoopGroup = NioEventLoopGroup()

    private val eventBus = EventBus<ConnectionEvent>()

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
                state = ConnectionState.CONNECTING
                internalsHandler.sendInternal(ConnectionRequestAccepted(
                    clientAddress = this.address,
                    systemIndex = 0,
                    requestTime = packet.time,
                    time = server.getUptime()
                ))
            }
            is NewIncomingConnection -> {
                worker.scheduleAtFixedRate(this::ping, TimeComponent.PING.toLong(), TimeComponent.PING.toLong(), TimeUnit.MILLISECONDS)
                state = ConnectionState.CONNECTED
                eventBus.dispatch(ConnectionEvent.Connected)
            }
            is ConnectedPing -> internalsHandler.sendInternal(ConnectedPong(
                pingTime = packet.time,
                pongTime = server.getUptime()
            ))
            is ConnectedPong -> {
                // Compute latency
                val latency = server.getUptime() - packet.pingTime
                eventBus.dispatch(ConnectionEvent.LatencyUpdated(latency))
            }
            is DisconnectionNotification -> close(DisconnectionReason.ClientRequested)
            is UserMessage -> {
                eventBus.dispatch(ConnectionEvent.Received(packet))
                packet.buffer.release()
            }
        }
    }

    fun send(packet: OnlineMessage, immediate: Boolean = false) = internalsHandler.send(packet, immediate)

    fun ping() {
        if(state !== ConnectionState.CONNECTED) {
            return
        }
        send(ConnectedPing(time = server.getUptime()), true)
    }

    fun getEventBus() = eventBus

    fun close(reason: DisconnectionReason) {
        worker.shutdownGracefully()
        server.removeConnection(this)
        eventBus.dispatch(ConnectionEvent.Disconnected(reason))
        state = ConnectionState.DISCONNECTED
    }

}