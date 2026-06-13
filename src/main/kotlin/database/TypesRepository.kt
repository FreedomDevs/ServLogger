package dev.elysium.servlogger.database

class TypesRepository (val database: ServLoggerDatabase){
    companion object {
        private const val getIdByIdentifierSql = "SELECT id FROM types WHERE identifier = ?;"
        private const val createByIdentifierSql = "INSERT INTO types (identifier) VALUES (?);"
        private const val getIdentifierByIdSql = "SELECT identifier FROM types WHERE id = ?;"
    }

    fun getIdByIdentifier(identifier: String): Long? {
        database.connection.prepareStatement(getIdByIdentifierSql).use { stmt ->
            stmt.setString(1, identifier)
            stmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getLong("id")
                }
                return null
            }
        }
    }

    fun createByIdentifier(identifier: String): Long {
        database.connection.prepareStatement(createByIdentifierSql).use { stmt ->
            stmt.setString(1, identifier)
            stmt.executeUpdate()

            stmt.generatedKeys.use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        throw RuntimeException("Couldn't read generated ID")
    }

    fun getOrCreateByIdentifier(identifier: String): Long {
        val id = getIdByIdentifier(identifier) ?: return createByIdentifier(identifier)
        return id
    }

    fun getIdentifierById(id: Long): String? {
        database.connection.prepareStatement(getIdentifierByIdSql).use { stmt ->
            stmt.setLong(1, id)
            stmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getString("identifier")
                }
                return null
            }
        }
    }
}