package raknet.enums

enum class AddressCount(private val count: Int) {
    RAKNET(10),
    MINECRAFT(20);

    fun toInt() = count
}