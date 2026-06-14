package dev.elysium.servlogger.database

import java.util.UUID

class UsersRepository(val database: ServLoggerDatabase) {
    companion object {
        private const val getIdByUniqueIdSql = "SELECT id FROM users WHERE uniqueId = ?"
        private const val getNicknameByUniqueIdSql = "SELECT nickname FROM users WHERE uniqueId = ?"
        private const val getIdAndNicknameByUniqueIdSql = "SELECT id,nickname FROM users WHERE uniqueId = ?"

        private const val createUserSql = "INSERT INTO users (uniqueId, nickname) VALUES (?, ?)"
        private const val updateUserNicknameSql = "UPDATE users SET nickname = ? WHERE id = ?"

        private const val getIdentifierByIdSql = "SELECT uniqueId FROM users WHERE id = ?"
    }

    fun getIdByUniqueId(uniqueId: UUID): Long? {
        database.getSqliteConnection()!!.prepareStatement(getIdByUniqueIdSql).use { stmt ->
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
        database.getSqliteConnection()!!.prepareStatement(getNicknameByUniqueIdSql).use { stmt ->
            stmt.setString(1, uniqueId.toString())
            stmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getString("nickname")
                }
                return null
            }
        }
    }

    fun getOrCreateUser(uniqueId: UUID, nickname: String, ignoreIfExists: Boolean = false): Long {
        database.getSqliteConnection()!!.prepareStatement(getIdAndNicknameByUniqueIdSql).use { stmt ->
            stmt.setString(1, uniqueId.toString())
            stmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    val id = resultSet.getLong("id")
                    val nickname1 = resultSet.getString("nickname")
                    if (nickname1 == nickname)
                        return id

                    database.getSqliteConnection()!!.prepareStatement(updateUserNicknameSql).use { stmt ->
                        stmt.setString(1, nickname)
                        stmt.executeUpdate()
                    }
                    return id
                }

                database.getSqliteConnection()!!.prepareStatement(createUserSql).use { stmt ->
                    stmt.setString(1, uniqueId.toString())
                    stmt.setString(2, nickname)
                    stmt.executeUpdate()

                    stmt.generatedKeys.use { resultSet ->
                        if (resultSet.next()) {
                            return resultSet.getLong(1)
                        }
                    }
                }
            }
        }

        throw RuntimeException("Couldn't read generated ID")
    }

    fun getUniqueIdById(id: Long): UUID? {
        database.getSqliteConnection()!!.prepareStatement(getIdentifierByIdSql).use { stmt ->
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
        database.getSqliteConnection()!!.prepareStatement(getIdentifierByIdSql).use { stmt ->
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