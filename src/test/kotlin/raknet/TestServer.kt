package raknet


fun main(args: Array<String>) {
    val server = Server(
        port = 19132,
        name = "Test Server",
        verbose = true
    )
    server.start()
}