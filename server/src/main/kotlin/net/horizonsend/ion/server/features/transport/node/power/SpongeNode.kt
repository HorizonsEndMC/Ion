package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.node.getNeighborNodes
import net.horizonsend.ion.server.features.transport.node.nodes.MultiNode
import net.horizonsend.ion.server.features.transport.node.nodes.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ALL_DIRECTIONS
import org.bukkit.block.BlockFace

/**
 * Represents a sponge [omnidirectional pipe]
 *
 * Since there is no use in keeping the individual steps, all touching sponges are consolidated into a single node with multiple inputs / outputs, weighted evenly
 **/
class SpongeNode(origin: Long) : MultiNode {
	override val positions: MutableSet<Long> = LongOpenHashSet()

	override val transferableNeighbors: MutableSet<TransportNode> = ObjectOpenHashSet()

	override fun isTransferable(position: Long, node: TransportNode): Boolean {
		return true
	}

	override fun shouldSplit(position: Long, nodes: MutableMap<Long, TransportNode>): Boolean {
		// If it covers a single or two blocks it cannot be split, simply remove it
		if (positions.size < 2) return false

		// Get neighbors by blockface relation
		val neighborPositions: Map<BlockFace, TransportNode> = getNeighborNodes(position, nodes)

		// If covered removing this node results in an equal number of neighbors and positions then none are touching
		if (neighborPositions.size == (positions.size - 1)) return true

		// All 26 adjacent directions
		val adjacentNeighborPositions = getNeighborNodes(position, positions, ALL_DIRECTIONS)

		if (adjacentNeighborPositions)
	}

	init {
		positions.add(origin)
	}
}