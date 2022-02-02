package raknet.packet

/**
 * Used as a way to separate unconnected packets from connected packets.
 */
abstract class ConnectedPacket(id: Int) : DataPacket(id)