package dev.elysium.servlogger.utils

import dev.elysium.servlogger.ServLogger
import org.bukkit.block.data.BlockData

object ParseBlockData {
    fun parseBlockData(blockData: BlockData): Pair<Long, Long?> {
        val asString = blockData.asString

        val materialKey = asString.substringBefore("[")
        val blockDataStr = asString.substringAfter("[", missingDelimiterValue = "").substringBefore("]")

        val blockId = ServLogger.instance.database.types.getOrCreateByIdentifier(materialKey)
        if (blockDataStr == "") {
            return Pair(blockId, null)
        }

        val blockDataId = ServLogger.instance.database.blockData.getOrCreateByBlockData(blockDataStr)
        return Pair(blockId, blockDataId)
    }
}