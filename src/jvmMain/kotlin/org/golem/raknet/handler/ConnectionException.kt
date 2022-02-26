package org.golem.raknet.handler

/**
 * This is thrown when something happens during a connection
 */
class ConnectionException(message: String): RuntimeException(message)