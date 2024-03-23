package net.horizonsend.ion.server.features.transport.grid.node.power

import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.grid.node.GridNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

abstract class PowerInputNode(
	x: Int,
	y: Int,
	z: Int,
	neighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap()
) : PowerNode(x, y, z, neighbors) {
	/**
	 * Gets the multiblocks to which this can input power
	 **/
	suspend fun getMultiblocks(): Collection<PoweredMultiblockEntity> = TODO()

	companion object {
		/**
		 * The offsets at which a multiblock sign might be found.
		 **/
		private val offsets = setOf(
			// most multiblocks have the sign a block up and out of the computer
			Vec3i(1, 1, 0), Vec3i(-1, 1, 0), Vec3i(0, 1, -1), Vec3i(0, 1, 1),
			// power cells have it on the block
			Vec3i(1, 0, 0), Vec3i(-1, 0, 0), Vec3i(0, 0, -1), Vec3i(0, 0, 1),
			// drills have it on a corner
			Vec3i(-1, 0, -1), Vec3i(1, 0, -1), Vec3i(1, 0, 1), Vec3i(-1, 0, 1),
			// upside down mining lasers have signs below
			Vec3i(1, -1, 0), Vec3i(-1, -1, 0), Vec3i(0, -1, -1), Vec3i(0, -1, 1),
		)
	}
}
