package net.horizonsend.ion.server.features.transport.node.general

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.transport.grid.Grid
import net.horizonsend.ion.server.features.transport.node.Consolidatable
import net.horizonsend.ion.server.features.transport.node.GridNode
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.faces
import net.horizonsend.ion.server.miscellaneous.utils.iterator
import org.bukkit.Axis
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import java.util.concurrent.ConcurrentHashMap

class LinearNode(
	override val parentGrid: Grid,
	override val x: Int,
	override val y: Int,
	override val z: Int,
	val axis: Axis,
	val occupiedPositions: LongOpenHashSet = LongOpenHashSet.of(toBlockKey(x, y, z))
) : GridNode, Consolidatable {
	constructor(
		grid: Grid,
		x: Int,
		y: Int,
		z: Int,
		data: Directional,
		occupiedPositions: LongOpenHashSet = LongOpenHashSet.of(toBlockKey(x, y, z))
	) : this(grid, x, y, z, data.facing.axis, occupiedPositions)

	override val transferableNeighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap()

	override fun isTransferableTo(offset: BlockFace, node: GridNode): Boolean {
		// Only allow transfer to wires along the same axis
		return offset.axis == axis
	}

	override fun consolidate() {
		for (face in this.axis.faces) {
			val node = getNeighbor(face) ?: continue
			if (node !is LinearNode) continue

			// Eat the neighbor
			occupiedPositions.addAll(node.occupiedPositions)

			// Replace the neighbor with this
			node.replace(this)
		}
	}
}
