package dev.elysium.servlogger.database

import java.util.UUID

class UsersRepository(val database: ServLoggerDatabase) {
    companion object {
        private const val getIdByUniqueIdSql = "SELECT id FROM users WHERE uniqueId = ?;"
        private const val getNicknameByUniqueIdSql = "SELECT nickname FROM users WHERE uniqueId = ?;"

        private const val createUserSql = "INSERT INTO users (uniqueId) VALUES (?);"
        private const val getOrCreateUserSql =
            "INSERT INTO users (uniqueId, nickname) VALUES (?, ?) ON CONFLICT(uniqueId) DO UPDATE SET nickname = excluded.nickname;"

        private const val getIdentifierByIdSql = "SELECT uniqueId FROM users WHERE id = ?;"
    }

    fun getIdByUniqueId(uniqueId: UUID): Long? {
        database.connection.prepareStatement(getIdByUniqueIdSql).use { stmt ->
            stmt.setString(1, uniqueId.toString())
            stmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getLong("id")
                }
                return null
            }
        }
    }

    fun getNicknameByUniqueId(uniqueId: UUID): String? {
        database.connection.prepareStatement(getNicknameByUniqueIdSql).use { stmt ->
            stmt.setString(1, uniqueId.toString())
            stmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getString("nickname")
                }
                return null
            }
        }
    }

    fun createUser(uniqueId: UUID, nickname: String, ignoreIfExists: Boolean = false): Long {
        database.connection.prepareStatement(
            if (ignoreIfExists) {
                getOrCreateUserSql
            } else {
                createUserSql
            }
        ).use { stmt ->
            stmt.setString(1, uniqueId.toString())
            stmt.setString(2, nickname)
            stmt.executeUpdate()

            stmt.generatedKeys.use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        throw RuntimeException("Couldn't read generated ID")
    }

    fun getUniqueIdById(id: Long): UUID? {
        database.connection.prepareStatement(getIdentifierByIdSql).use { stmt ->
            stmt.setLong(1, id)
            stmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("uniqueId"))
                }
                return null
            }
        }
    }

    fun getNicknameById(id: Long): String? {
        database.connection.prepareStatement(getIdentifierByIdSql).use { stmt ->
            stmt.setLong(1, id)
            stmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getString("nickname")
                }
                return null
            }
        }
    }
}