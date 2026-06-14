package dev.elysium.servlogger.database.actions

@Suppress("ArrayInDataClass")
data class BlockInformation(val blockId: Long, val blockDataId: Long?, val blockTileState: ByteArray?, val blockContainerData: List<ContainerDataRow>)
