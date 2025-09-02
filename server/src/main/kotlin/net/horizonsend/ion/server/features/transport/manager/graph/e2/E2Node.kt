package net.horizonsend.ion.server.features.transport.manager.graph.e2

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.BlockFace

abstract class E2Node(location: BlockKey, type: TransportNodeType<*>) : TransportNode(location, type) {
	private lateinit var graph: E2Network

	override fun getNetwork(): TransportNetwork<*> = graph
	override fun setNetworkOwner(graph: TransportNetwork<*>) {
		this.graph = graph as E2Network
	}

	class E2Port(location: BlockKey) : E2Node(location, TransportNetworkNodeTypeKeys.E2_PORT.getValue()) {
		override fun isIntact(): Boolean? {
			return getBlock()?.blockData?.customBlock?.key == CustomBlockKeys.E2_PORT
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}
}
