package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.block.BlockFace

class FluidGraphEdge(
	override val nodeOne: TransportNode,
	override val nodeTwo: TransportNode
) : GraphEdge {
	/** returns the net flow between the two nodes. If positive, it is towards node two, if negative, towards node one. */
	var netFlow: Double = 0.0

	val direction: BlockFace by lazy {
		val direction = toVec3i(nodeTwo.location).minus(toVec3i(nodeOne.location))
		val facesByMod = ADJACENT_BLOCK_FACES.plus(BlockFace.SELF).associateBy { face -> Vec3i(face.modX, face.modY, face.modZ) }
		facesByMod[direction]!!
	}
}
