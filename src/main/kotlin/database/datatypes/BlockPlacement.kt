package dev.elysium.servlogger.database.datatypes

data class BlockPlacement(
    val userId: Long,
    val x: Int,
    val y: Int,
    val z: Int,
    val worldId: Long,
    val placedBlockId: Long,
    val placedBlockDataId: Long?,
    val placedBlockTileState: ByteArray?,
    val placedBlockContainerData: List<ContainerDataRow>,
    val replacedBlockId: Long,
    val replacedBlockDataId: Long?,
    val replacedBlockTileState: ByteArray?,
    val replacedBlockContainerData: List<ContainerDataRow>
)
