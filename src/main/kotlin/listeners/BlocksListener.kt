package dev.elysium.servlogger.listeners

import dev.elysium.servlogger.ServLogger
import dev.elysium.servlogger.database.datatypes.BlockPlacement
import dev.elysium.servlogger.database.datatypes.ContainerDataRow
import dev.elysium.servlogger.utils.ParseBlockData
import org.bukkit.block.Container
import org.bukkit.block.TileState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlocksListener : Listener {
    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val userId = ServLogger.instance.database.users.createUser(event.player.uniqueId, event.player.name, true)
        val block = event.blockPlaced
        val worldId = ServLogger.instance.database.worlds.getOrCreateByIdentifier(block.world.name)

        val placedBlockIds = ParseBlockData.parseBlockData(block.blockData)
        var placedBlockTileState: ByteArray? = null
        val placedBlockContainerData: MutableList<ContainerDataRow> = mutableListOf()

        val placedBlockState = block.state
        if (placedBlockState is TileState) {
            if (!placedBlockState.persistentDataContainer.isEmpty) {
                placedBlockTileState = placedBlockState.persistentDataContainer.serializeToBytes()
            }

            if (placedBlockState is Container) {
                for ((slotIndex, item) in placedBlockState.inventory.withIndex()) {
                    if (item == null || item.type.isAir) {
                        continue
                    }

                    val itemId = ServLogger.instance.database.types.getOrCreateByIdentifier(item.type.key.toString())
                    placedBlockContainerData.add(
                        ContainerDataRow(
                            itemId,
                            item.amount,
                            slotIndex,
                            item.persistentDataContainer.serializeToBytes()
                        )
                    )
                }
            }
        }


        val replacedBlockIds = ParseBlockData.parseBlockData(event.blockReplacedState.blockData)
        var replacedBlockTileState: ByteArray? = null
        val replacedBlockContainerData: MutableList<ContainerDataRow> = mutableListOf()

        val replacedBlockState = event.blockReplacedState
        if (replacedBlockState is TileState) {
            if (!replacedBlockState.persistentDataContainer.isEmpty) {
                replacedBlockTileState = replacedBlockState.persistentDataContainer.serializeToBytes()
            }

            if (replacedBlockState is Container) {
                for ((slotIndex, item) in replacedBlockState.inventory.withIndex()) {
                    if (item == null || item.type.isAir) {
                        continue
                    }

                    val itemId = ServLogger.instance.database.types.getOrCreateByIdentifier(item.type.key.toString())
                    replacedBlockContainerData.add(
                        ContainerDataRow(
                            itemId,
                            item.amount,
                            slotIndex,
                            item.persistentDataContainer.serializeToBytes()
                        )
                    )
                }
            }
        }

        val blockPlacement = BlockPlacement(
            userId,
            block.x,
            block.y,
            block.z,
            worldId,
            placedBlockIds.first,
            placedBlockIds.second,
            placedBlockTileState,
            placedBlockContainerData,
            replacedBlockIds.first,
            replacedBlockIds.second,
            replacedBlockTileState,
            replacedBlockContainerData
        )
    }
}