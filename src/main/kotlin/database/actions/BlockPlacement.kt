package dev.elysium.servlogger.database.actions

data class BlockPlacement(
    val userId: Long,
    val timestamp: Long,
    val x: Int,
    val y: Int,
    val z: Int,
    val worldId: Long,
    val placedBlock: BlockInformation,
    val replacedBlock: BlockInformation
) : LogAction() {

}
