package raknet.connection

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.nio.NioEventLoopGroup
import raknet.Server
import raknet.packet.DataPacket
import raknet.packet.protocol.*
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

enum class TimePeriod(val period: Long) { UPDATE(10), ACK(50), PING(2_000L), STALE(5_000L), TIMEOUT(30_000L) }
enum class State { INITIALIZING, CONNECTING, CONNECTED, DISCONNECTED }
enum class Priority { LOW, MEDIUM, HIGH, IMMEDIATE }

class Connection(
    val address: InetSocketAddress,
    private val server: Server,
    private val context: ChannelHandlerContext,
    private val mtuSize: Short,
    private val guid: Long,
) {

    private var state: State = State.INITIALIZING
    // TODO: Listeners
    private var worker: NioEventLoopGroup = NioEventLoopGroup()
    private val packetQueue: MutableList<DataPacket> = mutableListOf()

    init {
        worker.scheduleAtFixedRate(this::tick, 0, TimePeriod.UPDATE.period, TimeUnit.MILLISECONDS)
    }

    fun log(message: String, level: String = "INFO") {
        server.log("[Connection: $address] $message", level)
    }

    private fun tick() {
        if(packetQueue.size > 0) {
            log("Sending ${packetQueue.size} packets")
            packetQueue.forEach {
                context.write(it)
            }
            context.flush()
            packetQueue.clear()
        }

    }

    fun handle(packet: DataPacket) {
        log("Received packet: $packet")
        when(packet) {
            is ConnectionRequest -> {
                state = State.CONNECTING

                send(ConnectionRequestAccepted(
                    clientAddress = this.address,
                    systemIndex = 0,
                    requestTime = packet.time,
                    time = System.currentTimeMillis(),
                ), Priority.IMMEDIATE)
            }
            is NewIncomingConnection -> {
                state = State.CONNECTED
            }
            is DisconnectionNotification -> {
                server.closeConnection(this)
            }
            is ConnectedPing -> {
                log("Latency is " + (System.currentTimeMillis() - packet.time) + "ms")
                val response = ConnectedPong(
                    pingTime = packet.time,
                    pongTime = System.currentTimeMillis(),
                )
                send(response, Priority.IMMEDIATE)
            }
            else -> {}
        }
    }

    private fun send(packet: DataPacket, priority: Priority = Priority.MEDIUM) {
        if(priority == Priority.IMMEDIATE) {
            context.writeAndFlush(packet)
        } else {
            packetQueue.add(packet)
        }
    }

    private fun close(reason: String) {
        log("Closing connection: $reason")
        try {
            context.close()
            server.closeConnection(this)
            state = State.DISCONNECTED
        } catch(e: Exception) {
            e.printStackTrace()
        } finally {
            worker.shutdownGracefully()
        }
    }

}

