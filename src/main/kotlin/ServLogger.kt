package dev.elysium.servlogger

import dev.elysium.servlogger.database.ServLoggerDatabase
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class ServLogger : JavaPlugin() {
    companion object {
        lateinit var instance: ServLogger
            private set
        lateinit var logger: Logger
            private set
    }
    lateinit var database: ServLoggerDatabase

    override fun onLoad() {
        instance = this;
        Companion.logger = this.logger;
    }

    override fun onEnable() {
        database = ServLoggerDatabase()
        database.connect(dataFolder.toPath().resolve("database.db").toString())
        database.applyMigrations()
        logger.info("ServLogger enabled.")
    }

    override fun onDisable() {
        database.close()
        logger.info("ServLogger disabled.")
    }
}
