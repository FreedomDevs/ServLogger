package dev.elysium.servlogger.database

import dev.elysium.servlogger.database.actions.LogAction

interface LogHandler {
    val name: String
    val acceptedActions: List<Class<out LogAction>>
    fun process(actions: List<LogAction>, database: ServLoggerDatabase)
}