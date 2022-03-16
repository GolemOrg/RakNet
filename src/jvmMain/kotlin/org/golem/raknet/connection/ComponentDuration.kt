package org.golem.raknet.connection

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
enum class ComponentDuration(private val duration: Duration) {
    UPDATE(Duration.milliseconds(10)),
    PING(Duration.seconds(5)),
    TIMEOUT(Duration.seconds(30));

    fun toMilliseconds(): Long = duration.inWholeMilliseconds
}
