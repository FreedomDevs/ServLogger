package dev.elysium.servlogger.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.elysium.servlogger.database.ServLoggerDatabase
import dev.elysium.servlogger.database.actions.BlockInformation
import dev.elysium.servlogger.database.actions.ContainerDataRow
import io.papermc.paper.block.TileStateInventoryHolder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.block.Container
import org.bukkit.block.TileState
import org.bukkit.block.data.BlockData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

val gson = Gson()
object GameDataParsers {
    fun parseFullBlockInformation(database: ServLoggerDatabase, block: BlockState): BlockInformation {
        val ids = parseBlockData(database, block.blockData)

        var blockTileState: ByteArray? = null
        var blockContainerData = listOf<ContainerDataRow>()
        if (block is TileState) {
            blockTileState = TileStateSerializer.serializeTileState(database, block)

            if (block is TileStateInventoryHolder)
                blockContainerData = parseContainerData(database, block)
        }

        return BlockInformation(
            ids.first,
            ids.second,
            blockTileState,
            blockContainerData
        )
    }

    fun parseBlockData(database: ServLoggerDatabase, blockData: BlockData): Pair<Long, Long?> {
        val asString = blockData.asString

        val materialKey = asString.substringBefore("[")
        val blockDataStr = asString.substringAfter("[", missingDelimiterValue = "").substringBefore("]")

        val blockId = database.types.getOrCreateByIdentifier(materialKey)
        if (blockDataStr == "") {
            return Pair(blockId, null)
        }

        val blockDataId = database.blockData.getOrCreateByBlockData(blockDataStr)
        return Pair(blockId, blockDataId)
    }

    fun parseContainerData(database: ServLoggerDatabase, container: TileStateInventoryHolder): List<ContainerDataRow> {
        val result = mutableListOf<ContainerDataRow>()

        for ((slotIndex, item) in container.inventory.withIndex()) {
            if (item == null || item.type.isAir) {
                continue
            }

            val itemId = database.types.getOrCreateByIdentifier(item.type.key.toString())
            val meta: ByteArray? = serializeItemNBT(item)

            result.add(
                ContainerDataRow(
                    itemId,
                    item.amount,
                    slotIndex,
                    meta
                )
            )
        }

        return result
    }

    fun serializeItemNBT(item: ItemStack): ByteArray? {
        val tempConfig = YamlConfiguration()
        tempConfig.set("temp", item.clone())

        val testConfig = YamlConfiguration()
        testConfig.loadFromString(tempConfig.saveToString().replace("  ==: org.bukkit.inventory.ItemStack\n", "").replace("temp:\n", ""))

        testConfig.set("id", Unit)
        testConfig.set("count", Unit)

        val dataVersion = testConfig.getInt("DataVersion")
        val schemaVersion = testConfig.getInt("schema_version")
        testConfig.set("DataVersion", Unit)
        testConfig.set("schema_version", Unit)

        val componentsSection = testConfig.getConfigurationSection("components") ?: return null
        val componentsMap = componentsSection.getValues(true)

        val minifiedJson = gson.toJson(componentsMap)

        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzip ->
            val dos1 = DataOutputStream(gzip)
            dos1.writeInt(dataVersion)
            dos1.writeInt(schemaVersion)
            gzip.write(minifiedJson.toByteArray(Charsets.UTF_8))
        }

        return bos.toByteArray()
    }

    fun deserializeItemNBT(data: ByteArray?, itemIdFromDb: String, amountFromDb: Int): ItemStack {
        val material = Material.matchMaterial(itemIdFromDb) ?: throw RuntimeException("Item $itemIdFromDb not found")
        var item = ItemStack(material, amountFromDb)

        if (data == null || data.isEmpty()) {
            return item
        }

        try {
            val bais = ByteArrayInputStream(data)

            GZIPInputStream(bais).use { gzis ->
                val dis = DataInputStream(gzis)
                val dataVersionByte = dis.readInt()
                val schemaVersionByte = dis.readInt()
                val jsonString = gzis.bufferedReader(Charsets.UTF_8).use { it.readText() }

                val mapType = object : TypeToken<Map<String, Any>>() {}.type
                val componentsMap: Map<String, Any> = gson.fromJson(jsonString, mapType)

                val finalConfig = YamlConfiguration()
                finalConfig.set("result.==", "org.bukkit.inventory.ItemStack")
                finalConfig.set("result.id", itemIdFromDb)
                finalConfig.set("result.count", amountFromDb)

                finalConfig.set("result.DataVersion", dataVersionByte)
                finalConfig.set("result.schema_version",  schemaVersionByte)

                finalConfig.set("result.components", componentsMap)

                val restoredItem = finalConfig.get("result") as? ItemStack
                if (restoredItem != null) {
                    item = restoredItem
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return item
    }
}