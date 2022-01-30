package raknet

data class Identifier(val values: ArrayList<Any>) {

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(HEADER)
        builder.append(SEPARATOR)
        for (value in values) {
            builder.append(value)
            builder.append(SEPARATOR)
        }
        return builder.toString()
    }

    companion object {
        const val HEADER: String = "MCPE"
        const val SEPARATOR: Char = ';'
    }

}