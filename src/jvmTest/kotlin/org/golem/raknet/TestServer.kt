package org.golem.raknet

import io.netty.channel.nio.NioEventLoopGroup
import org.golem.raknet.connection.Connection
import org.golem.raknet.connection.ConnectionEvent
import org.golem.raknet.message.OnlineMessage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

fun main() {
    val server = PersonalServer (
        port = 19132,
        name = "Test Server"
    )
    server.start()
}

const val TICK_RATE = 20

class PersonalConnection(
    private val connection: Connection
) {

    init {
        connection.getEventBus().listen(this) {
            when(it) {
                is ConnectionEvent.Connected -> this.handleConnect()
                is ConnectionEvent.Disconnected -> this.handleDisconnect()
                is ConnectionEvent.Received -> this.handleMessage(it.message)
                is ConnectionEvent.LatencyUpdated -> {
                    log("Latency updated to ${it.latency}")
                }
            }
        }
    }

    private fun handleConnect() {
        log("Client connected")
    }

    private fun handleMessage(message: OnlineMessage) {
        log("Received message: $message")
    }

    private fun handleDisconnect() {
        log("Client disconnected")
        connection.getEventBus().remove(this)
    }

    fun log(message: String, level: String = "INFO") {
        println("[%s] [%s] [%s] %s".format(
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(Date()),
            "Connection: ${connection.address}",
            level,
            message
        ))
    }
}

class PersonalServer(
    port: Int,
    private val name: String,
    threadCount: Int = 4
) {
    val connections = mutableListOf<PersonalConnection>()
    val workerGroup = NioEventLoopGroup(threadCount)
    val guid = UUID.randomUUID()
    val raknet = Server(
        port = port,
        guid = guid,
        name = "MCPE;Golem Server;475;1.18.0;0;100;${guid.mostSignificantBits};Golem;Creative;1;19132;19132"
    )

    val running = AtomicBoolean(true)

    fun start() {
        log("Starting server...")

        log("Scheduling worker group...")
        workerGroup.scheduleAtFixedRate(this::tick, 0, 1000L / TICK_RATE, TimeUnit.MILLISECONDS)
        log("Starting RakNet...")
        raknet.getEventBus().listen { event ->
            when(event) {
                is ServerEvent.Start -> {
                    log("Server started successfully!")
                }
                is ServerEvent.NewConnection -> {
                    val connection = event.connection
                    log("New connection from ${connection.address}")
                    connections.add(PersonalConnection(connection))
                }
                is ServerEvent.Shutdown -> {
                    log("Server shutting down...")
                }
            }
        }
        raknet.start()
        while(running.get()) {
            if(!raknet.isAlive()) {
                log("RakNet died! Stopping server...")
                running.set(false)
                break
            }
            tick()
            // Sleep for a millisecond
            Thread.sleep(1)
        }
        raknet.shutdown()
        workerGroup.shutdownGracefully()
    }

    private fun tick() {

    }

    fun log(message: String, level: String = "INFO") {
        println("[%s] [%s] [%s] %s".format(
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(Date()),
            "Server",
            level,
            message
        ))
    }

    fun shutdown() {
        running.set(false)
    }

}