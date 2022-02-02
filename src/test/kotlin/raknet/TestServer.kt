package raknet

fun main() {
    val server = Server (
        port = 19132,
        name = "Test Server",
        verbose = true,
    )
    server.start()
}