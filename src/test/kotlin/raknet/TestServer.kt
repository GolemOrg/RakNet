package raknet

import io.netty.channel.nio.NioEventLoopGroup
import raknet.connection.Connection
import raknet.connection.ConnectionListener
import raknet.packet.DataPacket
import raknet.packet.protocol.*
import java.net.InetSocketAddress
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun main() {
    val server = PersonalServer (
        port = 19132,
        name = "Test Server",
        verbose = true,
        motd = MOTD(
            message = "Test Server",
            protocolVersion = 475,
            gameVersion = "1.18.0",
            playersOnline = 0,
            maxPlayers = 100,
            guid = UUID.randomUUID().mostSignificantBits,
            subMotd = "Golem",
            gamemode = "Creative",
            gamemodeNumeric = 1,
            ipv4Port = 19132,
            ipv6Port = 19132,
        )
    )
    server.start()
}

const val TICK_RATE = 20

data class MOTD(
    val message: String,
    val protocolVersion: Int,
    val gameVersion: String,
    val playersOnline: Int,
    val maxPlayers: Int,
    var guid: Long,
    val subMotd: String,
    val gamemode: String,
    val gamemodeNumeric: Int,
    val ipv4Port: Int,
    val ipv6Port: Int
) {

    fun build() = Identifier(
        message,
        protocolVersion,
        gameVersion,
        playersOnline,
        maxPlayers,
        guid,
        subMotd,
        gamemode,
        gamemodeNumeric,
        ipv4Port,
        ipv6Port
    )
}

class PersonalServer(
    private val port: Int,
    private val name: String,
    private val verbose: Boolean = true,
    private var motd: MOTD,
    threadCount: Int = 4
) {
    val workerGroup = NioEventLoopGroup(threadCount)
    val raknet = Server(port = port, name = name)

    fun start() {
        motd.guid = raknet.guid.mostSignificantBits
        raknet.identifier = motd.build()

        raknet.listen(PersonalServerListener(this))

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

class PersonalServerListener(val server: PersonalServer): ServerListener() {

    override fun handleNewConnection(connection: Connection) {
        server.log("Received new connection from ${connection.address}")
        connection.listen(object: ConnectionListener() {
            override fun handlePacket(packet: DataPacket) {
                if(packet is UnknownPacket) {
                    server.log("Received unknown packet (${packet.id}) from ${connection.address}")
                    // Do stuff
                    packet.buffer.release()
                }
                // server.log("Received packet $packet from ${connection.address}")
            }

            override fun handleConnected(connection: Connection) = server.log("Connection established with ${connection.address}")

            override fun handleDisconnected(connection: Connection, reason: String) = server.log("Connection disconnected with ${connection.address} for reason '$reason'")
        })
    }
}