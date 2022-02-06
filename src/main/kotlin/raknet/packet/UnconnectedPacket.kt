package raknet.packet

abstract class UnconnectedPacket(override val id: Int): DataPacket(id)