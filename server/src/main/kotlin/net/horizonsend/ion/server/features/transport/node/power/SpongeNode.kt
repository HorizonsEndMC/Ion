package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.transport.network.PowerNetwork
import net.horizonsend.ion.server.features.transport.node.general.JunctionNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

/**
 * Represents a sponge [omnidirectional pipe]
 *
 * Since there is no use in keeping the individual steps, all touching sponges are consolidated into a single node with multiple inputs / outputs, weighted evenly
 **/
class SpongeNode(network: PowerNetwork) : JunctionNode<PowerNetwork, SpongeNode, SpongeNode>(network) {
	constructor(network: PowerNetwork, origin: BlockKey) : this(network) {
		positions.add(origin)
	}

	override suspend fun addBack(position: BlockKey) {
		network.nodeFactory.addSponge(position, handleRelationships = false)
	}

	override fun toString(): String = "(SPONGE NODE: ${positions.size} positions, Transferable to: ${getTransferableNodes().joinToString { it.first.javaClass.simpleName }} nodes)"
}
