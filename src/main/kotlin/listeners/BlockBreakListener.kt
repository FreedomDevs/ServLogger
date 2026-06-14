package dev.elysium.servlogger.listeners

import dev.elysium.servlogger.ServLogger
import dev.elysium.servlogger.database.actions.BlockPlacement
import dev.elysium.servlogger.utils.GameDataParsers
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

object BlockBreakListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val userId = ServLogger.instance.database.users.getOrCreateUser(event.player.uniqueId, event.player.name)
        val block = event.blockPlaced
        val worldId = ServLogger.instance.database.worlds.getOrCreateByIdentifier(block.world.name)

        val placedBlockInfo = GameDataParsers.parseFullBlockInformation(ServLogger.instance.database, block.state)
        val replacedBlockInfo = GameDataParsers.parseFullBlockInformation(ServLogger.instance.database, event.blockReplacedState)

        val blockPlacement = BlockPlacement(
            userId,
            System.currentTimeMillis(),
            block.x,
            block.y,
            block.z,
            worldId,
            placedBlockInfo,
            replacedBlockInfo
        )

        ServLogger.instance.database.logQueue.addToQueue(blockPlacement)
    }
}