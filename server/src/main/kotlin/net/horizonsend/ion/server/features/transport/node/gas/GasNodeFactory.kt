package net.horizonsend.ion.server.features.transport.node.gas

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.network.GasNetwork
import net.horizonsend.ion.server.features.transport.node.NodeFactory
import net.horizonsend.ion.server.features.transport.node.getNeighborNodes
import net.horizonsend.ion.server.features.transport.node.handleMerges
import net.horizonsend.ion.server.features.transport.node.power.SpongeNode
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.faces
import org.bukkit.Material
import org.bukkit.block.data.Directional

class GasNodeFactory(network: GasNetwork) : NodeFactory<GasNetwork>(network) {
	override suspend fun create(key: BlockKey, snapshot: BlockSnapshot) {
		if (network.nodes.contains(key)) return

		when {
			// Straight wires
			snapshot.type == Material.END_ROD -> addLightningRod(snapshot.data as Directional, key)

			// Omnidirectional wires
			snapshot.type == Material.SPONGE -> addJunction(key)
		}
	}

	suspend fun addLightningRod(data: Directional, position: Long, handleRelationships: Boolean = true) {
		val axis = data.facing.axis

		// The neighbors in the direction of the wire's facing, that are also facing that direction
		val neighbors = getNeighborNodes(position, network.nodes, axis.faces.toList())
			.values
			.filterIsInstance<LightningRodNode>()
			.filterTo(mutableListOf()) { it.axis == axis }

		val finalNode = when (neighbors.size) {
			// Disconnected
			0 ->  LightningRodNode(network, position, data.facing.axis).apply { loadIntoNetwork() }

			// Consolidate into neighbor
			1 -> neighbors.firstOrNull()?.addPosition(position) ?: throw ConcurrentModificationException("Node removed during processing")

			// Should be a max of 2
			2 -> handleMerges(neighbors).addPosition(position)

			else -> throw IllegalArgumentException("Linear node had more than 2 neighbors")
		}

		if (handleRelationships) finalNode.rebuildRelations()
	}

	suspend fun addJunction(position: BlockKey, handleRelationships: Boolean = true) {
		val neighbors = getNeighborNodes(position, network.nodes).values.filterIsInstanceTo<SpongeNode, MutableList<SpongeNode>>(mutableListOf())

		val finalNode = when (neighbors.size) {
			// New sponge node
			0 -> GasJunctionNode(network, position).apply { loadIntoNetwork() }

			// Consolidate into neighbor
			1 ->  neighbors.firstOrNull()?.addPosition(position) ?: throw ConcurrentModificationException("Node removed during processing")

			// Join multiple neighbors together
			in 2..6 -> handleMerges(neighbors).addPosition(position)

			else -> throw NotImplementedError()
		}

		if (handleRelationships) finalNode.rebuildRelations()
	}
}
