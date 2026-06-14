package dev.elysium.servlogger.database.handlers

import dev.elysium.servlogger.ServLogger
import dev.elysium.servlogger.database.ServLoggerDatabase
import dev.elysium.servlogger.database.LogHandler
import dev.elysium.servlogger.database.actions.BlockBreak
import dev.elysium.servlogger.database.actions.LogAction
import java.sql.Types

object BlockBreakHandler : LogHandler {
    override val name = "BlockBreakHandler"
    override val acceptedActions: List<Class<out LogAction>> = listOf(BlockBreak::class.java)

    const val addBlockPlacementSql =
        "INSERT INTO block_breaks (userId, timestamp, x, y, z, worldId, blockId, blockDataId, blockNBT, blockContainerData) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

    override fun process(
        actions: List<LogAction>,
        database: ServLoggerDatabase
    ) {
        @Suppress("UNCHECKED_CAST")
        val actions = actions as List<BlockBreak>

        val connection = database.getSqliteConnection()
            ?: throw RuntimeException("Error while logging block break: database is not defined or not sqlite")

        connection.prepareStatement(addBlockPlacementSql).use { stmt ->
            for (action in actions) {
                stmt.setLong(1, action.userId)
                stmt.setLong(2, action.timestamp)
                stmt.setInt(3, action.x)
                stmt.setInt(4, action.y)
                stmt.setInt(5, action.z)
                stmt.setLong(6, action.worldId)

                stmt.setLong(7, action.block.blockId)
                if (action.block.blockDataId != null)
                    stmt.setLong(8, action.block.blockDataId)
                else
                    stmt.setNull(8, Types.INTEGER)

                if (action.block.blockTileState != null)
                    stmt.setBytes(9, action.block.blockTileState)
                else
                    stmt.setNull(9, Types.BLOB)

                if (!action.block.blockContainerData.isEmpty())
                    stmt.setLong(10, database.containerItemsData.createContainerData(action.block.blockContainerData))
                else
                    stmt.setNull(10, Types.INTEGER)

                stmt.addBatch()
            }

            ServLogger.debugLog("Executing block break: $stmt")
            stmt.executeBatch()
        }
    }
}