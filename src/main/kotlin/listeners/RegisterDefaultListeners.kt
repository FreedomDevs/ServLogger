package dev.elysium.servlogger.listeners

import dev.elysium.servlogger.ServLogger
import org.bukkit.Bukkit
import org.bukkit.event.Listener

object RegisterDefaultListeners {
    val defaultListeners: List<Listener> = listOf(
        BlockPlaceListener,
        BlockBreakListener,
    )

    fun registerDefaultListeners(plugin: ServLogger) {
        for (handler in defaultListeners) {
            Bukkit.getPluginManager().registerEvents(handler, plugin)
        }
    }
}