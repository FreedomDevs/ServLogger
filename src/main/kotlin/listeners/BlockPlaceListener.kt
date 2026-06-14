package dev.elysium.servlogger.listeners

import dev.elysium.servlogger.ServLogger
import dev.elysium.servlogger.database.actions.BlockBreak
import dev.elysium.servlogger.database.actions.BlockPlacement
import dev.elysium.servlogger.utils.GameDataParsers
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

object BlockPlaceListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val userId = ServLogger.instance.database.users.getOrCreateUser(event.player.uniqueId, event.player.name)
        val block = event.block.state
        val worldId = ServLogger.instance.database.worlds.getOrCreateByIdentifier(block.world.name)

        val placedBlockInfo = GameDataParsers.parseFullBlockInformation(ServLogger.instance.database, block)
        val blockBreak = BlockBreak(
            userId,
            System.currentTimeMillis(),
            block.x,
            block.y,
            block.z,
            worldId,
            placedBlockInfo,
        )

        ServLogger.instance.database.logQueue.addToQueue(blockBreak)
    }
}