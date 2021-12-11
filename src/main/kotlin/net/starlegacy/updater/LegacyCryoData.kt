package net.starlegacy.updater

import java.util.UUID

data class LegacyCryoData(
    val uuid: UUID,
    val world: String,
    val x: Int,
    val y: Int,
    val z: Int
)
