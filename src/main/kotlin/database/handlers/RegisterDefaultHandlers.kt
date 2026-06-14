package dev.elysium.servlogger.database.handlers

import dev.elysium.servlogger.database.LogHandler
import dev.elysium.servlogger.database.ServLoggerDatabase

object RegisterDefaultHandlers {
    val defaultHandlers: List<LogHandler> = listOf(
        BlockPlacementHandler,
        BlockBreakHandler,
    )

    fun registerDefaultHandlers(database: ServLoggerDatabase) {
        for (handler in defaultHandlers) {
            database.logQueue.registerHandler(handler)
        }
    }

    fun unregisterDefaultHandlers(database: ServLoggerDatabase) {
        for (handler in defaultHandlers) {
            database.logQueue.unregisterHandler(handler)
        }
    }
}