package dev.elysium.servlogger.database

class BlockDataRepository (val database: ServLoggerDatabase){
    companion object {
        private const val getIdByBlockDataSql = "SELECT id FROM blocks_data WHERE block_data = ?;"
        private const val createByBlockDataSql = "INSERT INTO blocks_data (block_data) VALUES (?);"
        private const val getBlockDataByIdSql = "SELECT block_data FROM blocks_data WHERE id = ?;"
    }

    fun getIdByBlockData(blockData: String): Long? {
        database.getSqliteConnection()!!.prepareStatement(getIdByBlockDataSql).use { stmt ->
            stmt.setString(1, blockData)
            stmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getLong("id")
                }
                return null
            }
        }
    }

    fun createByBlockData(blockData: String): Long {
        database.getSqliteConnection()!!.prepareStatement(createByBlockDataSql).use { stmt ->
            stmt.setString(1, blockData)
            stmt.executeUpdate()

            stmt.generatedKeys.use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        throw RuntimeException("Couldn't read generated ID")
    }

    fun getOrCreateByBlockData(blockData: String): Long {
        val id = getIdByBlockData(blockData) ?: return createByBlockData(blockData)
        return id
    }

    fun getBlockDataById(id: Long): String? {
        database.getSqliteConnection()!!.prepareStatement(getBlockDataByIdSql).use { stmt ->
            stmt.setLong(1, id)
            stmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getString("blocks_data")
                }
                return null
            }
        }
    }
}