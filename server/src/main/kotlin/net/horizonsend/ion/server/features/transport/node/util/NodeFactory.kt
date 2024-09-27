package net.horizonsend.ion.server.features.transport.node.util

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.features.transport.node.type.general.DirectionalNode
import net.horizonsend.ion.server.features.transport.node.type.general.JunctionNode
import net.horizonsend.ion.server.features.transport.node.type.general.LinearNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.faces
import org.bukkit.Axis
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData

abstract class NodeFactory<T: NodeManager>(val network: T) {
	/**
	 * Create and handle placement of a node at the position, if one should be created
	 **/
	abstract fun create(key: BlockKey, data: BlockData): Boolean

	inline fun <reified T: JunctionNode<*, T, T>> addJunctionNode(position: BlockKey, type: NodeType, handleRelationships: Boolean = true) {
		val neighbors = getNeighborNodes(position, network.nodes).values.filterIsInstanceTo<T, MutableList<T>>(mutableListOf())

		val finalNode = when (neighbors.size) {
			0 -> newNode<T>(type).addPosition(position) // New sponge node
			1 -> neighbors.firstOrNull()?.addPosition(position) ?: newNode<T>(type).addPosition(position) // Consolidate into neighbor
			in 2..6 -> handleMerges(neighbors).addPosition(position) // Join multiple neighbors together

			else -> throw NotImplementedError()
		}
		if (handleRelationships) finalNode.rebuildRelations()
	}

	inline fun <reified T: LinearNode<*, T, T>> addLinearNode(position: BlockKey, axis: Axis, type: NodeType, handleRelationships: Boolean = true) {
		// The neighbors in the direction of the wire's facing, that are also facing that direction
		val neighbors = getNeighborNodes(position, network.nodes, axis.faces.toList())
			.values
			.filterIsInstance<T>()
			.filterTo(mutableListOf()) { it.axis == axis && it.type == type }

		val finalNode = when (neighbors.size) {
			0 -> newNode<T>(type).addPosition(position).setAxis(axis)
			1 -> neighbors.firstOrNull()?.addPosition(position) ?: newNode<T>(type).addPosition(position).setAxis(axis)
			2 -> handleMerges(neighbors).addPosition(position)

			else -> throw IllegalArgumentException("Linear node had more than 2 neighbors")
		}

		if (handleRelationships) finalNode.rebuildRelations()
	}

	fun addSimpleSingleNode(position: BlockKey, type: NodeType) {
		val new = type.newInstance(network) as SingleNode

		new.position = position
		new.onPlace(position)

		network.nodes[position] = new
	}

	fun addDirectionalNode(position: BlockKey, direction: BlockFace, type: NodeType) {
		val new = type.newInstance(network) as DirectionalNode

		new.position = position
		new.direction = direction
		new.onPlace(position)

		network.nodes[position] = new
	}

	inline fun <reified T: TransportNode> newNode(type: NodeType): T = type.newInstance(network) as T
}
