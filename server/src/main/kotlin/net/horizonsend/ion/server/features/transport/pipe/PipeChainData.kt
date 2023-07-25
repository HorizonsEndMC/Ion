package net.horizonsend.ion.server.features.transport.pipe

import net.horizonsend.ion.server.features.transport.pipe.filter.FilterItemData
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.World
import org.bukkit.block.BlockFace

data class PipeChainData(
    val source: Vec3i,
    val extractor: Vec3i,
    val world: World,
    var x: Int,
    var y: Int,
    var z: Int,
    var direction: BlockFace,
    var distance: Int,
    var accumulatedFilter: Set<FilterItemData>
)
