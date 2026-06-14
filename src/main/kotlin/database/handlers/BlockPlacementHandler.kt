package dev.elysium.servlogger.database.handlers

import dev.elysium.servlogger.ServLogger
import dev.elysium.servlogger.database.ServLoggerDatabase
import dev.elysium.servlogger.database.LogHandler
import dev.elysium.servlogger.database.actions.BlockPlacement
import dev.elysium.servlogger.database.actions.LogAction

object BlockPlacementHandler : LogHandler {
    override val name = "BlockPlacementHandler"
    override val acceptedActions: List<Class<out LogAction>> = listOf(BlockPlacement::class.java)

    const val addBlockPlacementSql =
        "INSERT INTO block_placements (userId, timestamp, x, y, z, worldId, placedBlockId, placedBlockDataId, placedBlockNBT, placedBlockContainerData, replacedBlockId, replacedBlockDataId, replacedBlockNBT, replacedBlockContainerData) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

    override fun process(
        actions: List<LogAction>,
        database: ServLoggerDatabase
    ) {
        @Suppress("UNCHECKED_CAST")
        val actions = actions as List<BlockPlacement>

        val connection = database.getSqliteConnection()
            ?: throw RuntimeException("Error while logging block placement: database is not defined or not sqlite")

        connection.prepareStatement(addBlockPlacementSql).use { stmt ->
            for (action in actions) {
                stmt.setLong(1, action.userId)
                stmt.setLong(2, action.timestamp)
                stmt.setInt(3, action.x)
                stmt.setInt(4, action.y)
                stmt.setInt(5, action.z)
                stmt.setLong(6, action.worldId)

                stmt.setLong(7, action.placedBlock.blockId)
                if (action.placedBlock.blockDataId != null)
                    stmt.setLong(8, action.placedBlock.blockDataId)
                else
                    stmt.setNull(8, java.sql.Types.INTEGER)

                if (action.placedBlock.blockTileState != null)
                    stmt.setBytes(9, action.placedBlock.blockTileState)
                else
                    stmt.setNull(9, java.sql.Types.BLOB)

                if (!action.placedBlock.blockContainerData.isEmpty())
                    stmt.setLong(10, database.containerItemsData.createContainerData(action.placedBlock.blockContainerData))
                else
                    stmt.setNull(10, java.sql.Types.INTEGER)

                stmt.setLong(11, action.replacedBlock.blockId)
                if (action.replacedBlock.blockDataId != null)
                    stmt.setLong(12, action.replacedBlock.blockDataId)
                else
                    stmt.setNull(12, java.sql.Types.INTEGER)

                if (action.replacedBlock.blockTileState != null)
                    stmt.setBytes(13, action.replacedBlock.blockTileState)
                else
                    stmt.setNull(13, java.sql.Types.BLOB)

                if (!action.replacedBlock.blockContainerData.isEmpty())
                    stmt.setLong(14, database.containerItemsData.createContainerData(action.replacedBlock.blockContainerData))
                else
                    stmt.setNull(14, java.sql.Types.INTEGER)
                stmt.addBatch()
            }

            ServLogger.debugLog("Executing $stmt)")
            stmt.executeBatch()
        }
    }
}