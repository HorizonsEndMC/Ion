package net.horizonsend.ion.server.features.transport.grid

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

interface GridNode {
	val x: Int
	val y: Int
	val z: Int

	val key get() = toBlockKey(x, y, z)

	val neighbors: ConcurrentHashMap<BlockFace, GridNode>
}
