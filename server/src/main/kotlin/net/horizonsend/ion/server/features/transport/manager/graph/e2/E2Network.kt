package net.horizonsend.ion.server.features.transport.manager.graph.e2

import net.horizonsend.ion.server.features.transport.manager.graph.NetworkManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidGraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

class E2Network(uuid: UUID, override val manager: NetworkManager<E2Node, TransportNetwork<E2Node>>) : TransportNetwork<E2Node>(uuid, manager) {
	override fun createEdge(
		nodeOne: E2Node,
		nodeTwo: E2Node
	): GraphEdge {
		return FluidGraphEdge(nodeOne, nodeTwo)
	}

	override fun handleTick() {

	}

	override fun save(adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
		return adapterContext.newPersistentDataContainer()
	}
}
