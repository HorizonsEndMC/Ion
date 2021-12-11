package net.starlegacy.spacegenerator.asteroid

import net.starlegacy.util.Vec3i
import org.bukkit.block.data.BlockData

internal data class CachedAsteroid(
    val blocks: Map<Vec3i, BlockData>, // coordinates are relative to min x/y/z
    val width: Int,
    val height: Int,
    val length: Int
)
