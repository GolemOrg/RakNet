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

class Connection(
    val address: InetSocketAddress,
    val server: Server,
    context: ChannelHandlerContext,
    val mtuSize: Short,
    val guid: Long,
) {
    private val internalsHandler = InternalsHandler(this, context)
    private val worker: NioEventLoopGroup = NioEventLoopGroup()
    private val eventBus = EventBus<ConnectionEvent>()

    var state: ConnectionState = ConnectionState.INITIALIZING
        private set
    var latency: Long = 0
        private set

    val isConnected: Boolean
        get() = state != ConnectionState.DISCONNECTED


    init {
        worker.scheduleAtFixedRate(this::tick, 0, ComponentDuration.UPDATE.toMilliseconds(), TimeUnit.MILLISECONDS)
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
                worker.scheduleAtFixedRate(
                    this::ping,
                    ComponentDuration.PING.toMilliseconds(),
                    ComponentDuration.PING.toMilliseconds(),
                    TimeUnit.MILLISECONDS
                )
                state = ConnectionState.CONNECTED
                eventBus.dispatch(ConnectionEvent.Connected)
            }
            is ConnectedPing -> internalsHandler.sendInternal(ConnectedPong(
                pingTime = packet.time,
                pongTime = server.getUptime()
            ))
            is ConnectedPong -> {
                // Compute latency
                latency = server.getUptime() - packet.pingTime
                eventBus.dispatch(ConnectionEvent.LatencyUpdated(latency))
            }
            is DisconnectionNotification -> close(DisconnectionReason.ClientRequested)
            is UserMessage -> {
                eventBus.dispatch(ConnectionEvent.ReceivedMessage(packet))
                packet.buffer.release()
            }
        }
    }

    fun send(packet: OnlineMessage, immediate: Boolean = false) = internalsHandler.send(packet, immediate)

    fun ping() = send(ConnectedPing(time = server.getUptime()), true)

    fun getEventBus() = eventBus

    fun close(reason: DisconnectionReason) {
        worker.shutdownGracefully()
        server.removeConnection(this)
        eventBus.dispatch(ConnectionEvent.Disconnected(reason))
        state = ConnectionState.DISCONNECTED
    }

}