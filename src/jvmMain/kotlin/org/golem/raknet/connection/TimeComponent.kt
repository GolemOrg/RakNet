package org.golem.raknet.connection

enum class TimeComponent(private val millis: Long) {
    UPDATE(10),
    PING(5_000),
    TIMEOUT(30_000);

    fun toLong(): Long = millis
}
