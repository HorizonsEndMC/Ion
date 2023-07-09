package net.starlegacy.feature.transport.pipe

import net.starlegacy.feature.transport.pipe.filter.FilterItemData
import net.horizonsend.ion.server.miscellaneous.Vec3i
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
