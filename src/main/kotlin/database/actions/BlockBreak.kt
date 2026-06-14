package dev.elysium.servlogger.database.actions

data class BlockBreak(
    val userId: Long,
    val timestamp: Long,
    val x: Int,
    val y: Int,
    val z: Int,
    val worldId: Long,
    val block: BlockInformation,
) : LogAction() {

}
