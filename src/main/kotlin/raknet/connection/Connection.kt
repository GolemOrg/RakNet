package raknet.connection

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.nio.NioEventLoopGroup
import raknet.packet.Frame
import raknet.Server
import raknet.enums.Reliability
import raknet.handler.PacketEnvelope
import raknet.packet.*
import raknet.packet.protocol.*
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

enum class TimePeriod(val period: Long) {
    UPDATE(15), TIMEOUT(30_000L);

    fun check(time: Long): Boolean = System.currentTimeMillis() - time > period
}
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
    private val listeners: MutableList<ConnectionListener> = mutableListOf()
    private var worker: NioEventLoopGroup = NioEventLoopGroup()
    private val packetQueue: MutableList<DataPacket> = mutableListOf()
    private var currentSequenceIndex: Int = 0

    private var lastAckTime: Long = System.currentTimeMillis()
    private var lastDatagramTime: Long = System.currentTimeMillis()

    private var ackQueue: MutableList<Int> = mutableListOf()
    private var nackQueue: MutableList<Int> = mutableListOf()

    private val pendingPackets: MutableMap<Short, FragmentBuilder> = mutableMapOf()

    init {
        worker.scheduleAtFixedRate(this::tick, 0, TimePeriod.UPDATE.period, TimeUnit.MILLISECONDS)
    }

    private fun tick() {
        if(ackQueue.size > 0) {
            dispatchAck()
            ackQueue.clear()
        }
        if(nackQueue.size > 0) {
            dispatchNack()
            nackQueue.clear()
        }

        if(packetQueue.size > 0) {
            packetQueue.forEach { context.write(it) }
            context.flush()
            packetQueue.clear()
        }

        if(TimePeriod.TIMEOUT.check(lastDatagramTime)) this.close("Timeout")
    }

    fun listen(listener: ConnectionListener) = listeners.add(listener)

    private fun dispatchAck() {
        // Sort list of ACKs and create records
        val records = Record.fromList(ackQueue)
        ackQueue.clear()
        context.writeAndFlush(PacketEnvelope<ConnectedPacket>(Acknowledge(records), address))
    }

    private fun dispatchNack() {}

    fun handleAck(acknowledge: Acknowledge) {}

    fun handleNAck(nAcknowledge: NAcknowledge) {}

    fun handleDatagram(datagram: Datagram) {
        ackQueue.add(datagram.sequenceIndex.toInt())
        for (frame in datagram.frames) {
            val body = frame.body
            val frameType = body.readUnsignedByte().toInt()
            val packet: DataPacket = when(PacketType.find(frameType)) {
                PacketType.CONNECTION_REQUEST -> ConnectionRequest.from(body)
                PacketType.NEW_INCOMING_CONNECTION -> NewIncomingConnection.from(body)
                PacketType.DISCONNECTION_NOTIFICATION -> DisconnectionNotification()
                PacketType.CONNECTED_PING -> ConnectedPing.from(body)
                else -> {
                    if(frame.fragment != null) {
                        val builder = pendingPackets.getOrPut(frame.fragment.compoundId) { FragmentBuilder(frame.fragment.compoundSize) }
                        builder.add(frame.fragment, body)
                        if(!builder.complete()) {
                            continue
                        }

                        builder.build()
                    } else {
                        UnknownPacket(body.readUnsignedByte().toInt(), body.readBytes(body.readableBytes()))
                    }
                }
            }
            body.release()
            handle(packet)
        }
    }

    fun handle(packet: DataPacket) {
        listeners.forEach { it.handlePacket(packet) }
        when(packet) {
            is ConnectionRequest -> {
                state = State.CONNECTING
                val accepted = ConnectionRequestAccepted(
                    clientAddress = this.address,
                    systemIndex = 0,
                    requestTime = packet.time,
                    time = server.getUptime()
                )
                sendConnected(accepted)
            }
            is NewIncomingConnection -> {
                state = State.CONNECTED
            }
            is DisconnectionNotification -> {
                close("Client disconnect")
            }
            is ConnectedPing -> {
                val response = ConnectedPong(
                    pingTime = packet.time,
                    pongTime = server.getUptime()
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
        val prepared = packet.prepare()
        try {
            val datagram = Datagram(
                sequenceIndex = currentSequenceIndex++.toUInt(),
                frames = mutableListOf(
                    Frame(
                        reliability = reliability,
                        body = prepared
                    )
                )
            )
            context.writeAndFlush(PacketEnvelope<ConnectedPacket>(datagram, address))
        } finally {
            prepared.release()
        }
    }

    fun close(reason: String) {
        listeners.forEach { it.handleDisconnected(this, reason) }
        try {
            server.closeConnection(this)
            state = State.DISCONNECTED
        } finally {
            worker.shutdownGracefully()
            listeners.clear()
        }
    }

}