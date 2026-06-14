package dev.elysium.servlogger

import dev.elysium.servlogger.database.ServLoggerDatabase
import dev.elysium.servlogger.database.handlers.RegisterDefaultHandlers
import dev.elysium.servlogger.listeners.RegisterDefaultListeners
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class ServLogger : JavaPlugin() {
    companion object {
        lateinit var instance: ServLogger
            private set
        lateinit var logger: Logger
            private set

        fun debugLog(log: String) {
            logger.info ("DEBUG: $log")
        }
    }
    lateinit var database: ServLoggerDatabase

    override fun onLoad() {
        instance = this;
        dataFolder.mkdirs()
        Companion.logger = this.logger;
    }

    override fun onEnable() {
        database = ServLoggerDatabase()
        database.connect(dataFolder.toPath().resolve("database.db").toString())
        database.applyMigrations()

        RegisterDefaultHandlers.registerDefaultHandlers(database)
        RegisterDefaultListeners.registerDefaultListeners(this)

        server.scheduler.runTaskTimerAsynchronously(this, Runnable {
            database.logQueue.flushQueue()
        }, 20L, 20L)

        logger.info("ServLogger enabled.")
    }

    override fun onDisable() {
        database.logQueue.flushQueue()
        database.close()
        logger.info("ServLogger disabled.")
    }
}
