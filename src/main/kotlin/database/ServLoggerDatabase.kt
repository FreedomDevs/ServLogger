package dev.elysium.servlogger.database

import dev.elysium.servlogger.ServLogger
import org.bukkit.Bukkit
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


class ServLoggerDatabase {
    val databaseType = DatabaseTypes.SQLITE
    private lateinit var connection: Connection
    val worlds = WorldsRepository(this)
    val types = TypesRepository(this)
    val users = UsersRepository(this)
    val blockData = BlockDataRepository(this)
    val containerItemsData = ContainerItemsDataRepository(this)

    val logQueue = DatabaseQueue(this)

    @Suppress("RedundantNullableReturnType")
    fun getSqliteConnection(): Connection? {
        return connection
    }

    @UnsafeDatabaseApi
    fun getAnyConnection(): Connection {
        return connection
    }

    fun checkLastMigration(): Int {
        connection.createStatement().use { stmt ->
            stmt.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS migrations (
                    last_migration_id INTEGER NOT NULL
                );
            """.trimIndent()
            )
        }

        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT last_migration_id FROM migrations LIMIT 1;").use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getInt("last_migration_id")
                }
            }
        }

        val insertSql = "INSERT INTO migrations (last_migration_id) VALUES (0);"
        connection.createStatement().use { statement ->
            statement.execute(insertSql)
        }

        return 0
    }

    fun updateMigrationVersion(version: Int) {
        val deleteSql = "DELETE FROM migrations;"
        val insertSql = "INSERT INTO migrations (last_migration_id) VALUES (?);"

        try {
            connection.autoCommit = false

            connection.createStatement().use { stmt ->
                stmt.executeUpdate(deleteSql)
            }

            connection.prepareStatement(insertSql).use { pstmt ->
                pstmt.setInt(1, version)
                pstmt.executeUpdate()
            }

            connection.commit()
        } catch (e: Exception) {
            connection.rollback()
            throw RuntimeException("Couldn't update last migration id to v$version", e)
        } finally {
            connection.autoCommit = true
        }
    }

    fun applyMigrations() {
        val lastMigrationId = checkLastMigration()
        val lastExistingMigrationId = 1

        connection.autoCommit = false

        try {
            for (i in lastMigrationId + 1..lastExistingMigrationId) {
                ServLogger.logger.info("Applying migration $i")
                val sqlScript = readMigrationScript(i)

                connection.createStatement().use { statement ->
                    val rawStatements = sqlScript.split(";")

                    for (rawSql in rawStatements) {
                        val trimmedSql = rawSql.trim()
                        if (trimmedSql.isNotEmpty()) {
                            ServLogger.debugLog("Executing: $trimmedSql")
                            statement.addBatch(trimmedSql)
                        }
                    }

                    statement.executeBatch()
                }
            }
            connection.commit()
        } catch (e: Exception) {
            connection.rollback()
            connection.close()
            ServLogger.logger.info("Couldn't apply migrations")
            e.printStackTrace()
            Bukkit.getPluginManager().disablePlugin(ServLogger.instance)
        } finally {
            connection.autoCommit = true
        }

        updateMigrationVersion(lastExistingMigrationId)
    }

    fun connect(path: String) {
        try {
            val url = "jdbc:sqlite:$path"
            connection = DriverManager.getConnection(url)
            connection.createStatement().use { stmt ->
                stmt.execute("PRAGMA foreign_keys = ON;")
            }
            ServLogger.logger.info("SQLite connected!")
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    fun close() {
        try {
            connection.close()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private fun readMigrationScript(version: Int): String {
            val path = "migrations/migration_$version.sql"

            val inputStream = object {}.javaClass.classLoader.getResourceAsStream(path)
                ?: throw IllegalArgumentException("Migration file not found in resources: $path")

            return inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
    }
}