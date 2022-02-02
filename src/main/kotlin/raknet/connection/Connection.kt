package raknet.connection

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.nio.NioEventLoopGroup
import raknet.packet.Frame
import raknet.Server
import raknet.enums.Reliability
import raknet.handler.sendPacket
import raknet.packet.*
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
    private var currentSequenceIndex: Int = 0

    private var ackQueue: MutableList<Int> = mutableListOf()
    private var nackQueue: MutableList<Int> = mutableListOf()

    init {
        worker.scheduleAtFixedRate(this::tick, 0, TimePeriod.UPDATE.period, TimeUnit.MILLISECONDS)
    }

    fun log(message: String, level: String = "INFO") {
        server.log("[Connection: $address] $message", level)
    }

    private fun tick() {
//        if(ackQueue.size > 0) {
//            dispatchAck()
//            ackQueue.clear()
//        }
        if(packetQueue.size > 0) {
            log("Sending ${packetQueue.size} packets")
            packetQueue.forEach {
                context.write(it)
            }
            context.flush()
            packetQueue.clear()
        }
    }

    fun dispatchAck() {
        val single = ackQueue.size == 1
        val ack = Acknowledge(
            recordCount = ackQueue.size.toShort(),
            Record(
                isSingle = single,
                sequenceNumber = ackQueue.minOf { it }.toUInt(),
                endSequenceNumber = if(single) ackQueue.maxOf { it }.toUInt() else null
            )
        )
        context.sendPacket(address, ack)
    }

    fun handleAck(acknowledge: Acknowledge) {
    }

    fun handleNAck(nAcknowledge: NAcknowledge) {
    }

    fun handleDatagram(datagram: Datagram) {
        for (frame in datagram.frames) {
            if(frame.reliability.reliable()) {
                ackQueue.add(frame.reliableFrameIndex!!.toInt())
            }
            val body = frame.body
            val frameType = body.readUnsignedByte().toInt()
            val packet: DataPacket = when(PacketType.find(frameType)) {
                PacketType.CONNECTION_REQUEST -> ConnectionRequest.from(body)
                PacketType.NEW_INCOMING_CONNECTION -> NewIncomingConnection.from(body)
                PacketType.DISCONNECTION_NOTIFICATION -> DisconnectionNotification()
                PacketType.CONNECTED_PING -> ConnectedPing.from(body)
                else -> {
                    log("Received unknown packet of type $frameType")
                    continue
                }
            }
            handle(packet)
        }
    }

    fun handle(packet: DataPacket) {
        log("Received packet: $packet")
        when(packet) {
            is ConnectionRequest -> {
                dispatchAck()
                state = State.CONNECTING
                val accepted = ConnectionRequestAccepted(
                    clientAddress = this.address,
                    systemIndex = 0,
                    requestTime = packet.time,
                    time = System.currentTimeMillis(),
                )
                println("Connection accepted: $accepted")
                sendConnected(accepted)
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
                sendConnected(response)
            }
            else -> {}
        }
    }

    private fun send(packet: DataPacket, priority: Priority = Priority.MEDIUM) {
        TODO("Not implemented")
    }

    private fun sendConnected(packet: ConnectedPacket, reliability: Reliability = Reliability.UNRELIABLE) {
        val datagram = Datagram(
            sequenceIndex = currentSequenceIndex++.toUInt(),
            frames = mutableListOf(
                Frame(
                    reliability = reliability,
                    body = packet.prepare()
                )
            )
        )
        context.sendPacket(address, datagram)
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

