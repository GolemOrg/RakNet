package raknet

import io.netty.channel.nio.NioEventLoopGroup
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun main() {
    val server = PersonalServer (
        port = 19132,
        name = "Test Server"
    )
    server.start()
}

const val TICK_RATE = 20

class PersonalServer(
    private val port: Int,
    private val name: String,
    threadCount: Int = 4
) {
    val workerGroup = NioEventLoopGroup(threadCount)
    val guid = UUID.randomUUID()
    val raknet = Server(
        port = port,
        guid = guid,
    )

    fun start() {
        log("Starting server...")
        try {
            log("Creating worker group...")
            workerGroup.scheduleAtFixedRate(this::tick, 0, 1000L / TICK_RATE, TimeUnit.MILLISECONDS)
            log("Starting RakNet...")
            log("Server started successfully!") // TODO: If RakNet fails to start (e.g. port is in use), this shouldn't be printed :(
            raknet.start()
        } finally {
            workerGroup.shutdownGracefully()
        }
    }

    fun log(message: String, level: String = "INFO") {
        println("[%s] [%s] [%s] %s".format(
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date()),
            this::class.simpleName,
            level,
            message
        ))
    }

    fun shutdown() {
        raknet.shutdown()
        workerGroup.shutdownGracefully()
    }

    private fun tick() {

    }
}