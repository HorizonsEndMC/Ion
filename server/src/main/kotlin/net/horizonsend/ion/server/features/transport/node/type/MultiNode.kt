package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.grid.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.power.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative

/**
 * A transport node that may cover many blocks to avoid making unnecessary steps
 **/
interface MultiNode<Self: MultiNode<Self, T>, T: MultiNode<T, Self>> : TransportNode {
	/**
	 * The positions occupied by the node
	 **/
	val positions: MutableSet<BlockKey>

	suspend fun rebuildNode(network: ChunkTransportNetwork, position: BlockKey)

	override suspend fun handleRemoval(network: ChunkTransportNetwork, position: BlockKey) {
		network as ChunkPowerNetwork

		network.nodes.remove(position)
		positions.remove(position)

		// Remove all
		positions.forEach {
			network.nodes.remove(it)
		}

		rebuildNode(network, position)
	}

	/**
	 * Adds new a position to this node
	 **/
	fun addPosition(network: ChunkTransportNetwork, position: BlockKey) {
		positions += position
		network.nodes[position] = this
	}

	/**
	 * Adds new a position to this node
	 **/
	fun addPositions(network: ChunkTransportNetwork, newPositions: Iterable<BlockKey>) {
		for (position in newPositions) {
			positions += position
			network.nodes[position] = this
		}
	}

	/**
	 * Drain all the positions and connections to the provided node
	 **/
	fun drainTo(network: ChunkTransportNetwork, new: Self) {
		new.transferableNeighbors.addAll(transferableNeighbors)

		new.addPositions(network, positions)
	}

	override fun handlePlacement(network: ChunkTransportNetwork) {
		for (key in positions) {
			network.nodes[key] = this
		}
	}

	override suspend fun buildRelations(network: ChunkTransportNetwork, position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = network.nodes[offsetKey] ?: continue

			if (this == neighborNode) continue

			if (isTransferable(offsetKey, neighborNode)) {
				transferableNeighbors.add(neighborNode)
			}
		}
	}
}
