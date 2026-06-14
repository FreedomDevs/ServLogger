package dev.elysium.servlogger.database

import dev.elysium.servlogger.ServLogger
import dev.elysium.servlogger.database.actions.ContainerDataRow
import kotlin.use

class ContainerItemsDataRepository (val database: ServLoggerDatabase) {
    companion object {
        const val getLastContainerItemsDataId = "SELECT MAX(id) AS id FROM containers_items_data"
        const val addContainerItemDataSql = "INSERT INTO containers_items_data (id, itemId, amount, slot, itemNBT) VALUES (?, ?, ?, ?, ?)"
    }

    fun generateNewContainerItemsDataId(): Long {
        var lastId: Long = -1
        database.getSqliteConnection()!!.createStatement().use { stmt ->
            stmt.executeQuery(getLastContainerItemsDataId).use { resultSet ->
                if (resultSet.next()) {
                    lastId = resultSet.getLong("id")
                }
            }
        }

        return lastId + 1
    }

    fun createContainerData(containerData: List<ContainerDataRow>): Long {
        val id = generateNewContainerItemsDataId()

        database.getSqliteConnection()!!.prepareStatement(addContainerItemDataSql).use { stmt ->
            for (item in containerData) {
                stmt.setLong(1, id)
                stmt.setLong(2, item.itemId)
                stmt.setInt(3, item.amount)
                stmt.setInt(4, item.slot)

                if (item.itemNBT != null)
                    stmt.setBytes(5, item.itemNBT)
                else
                    stmt.setNull(5, java.sql.Types.BLOB)

                stmt.addBatch()
            }

            ServLogger.debugLog("Executing container data: $stmt)")
            stmt.executeBatch()
        }

        return id
    }
}