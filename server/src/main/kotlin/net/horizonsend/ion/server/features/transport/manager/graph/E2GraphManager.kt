package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.features.transport.manager.TransportHolder
import net.horizonsend.ion.server.features.transport.manager.graph.e2.E2Network
import net.horizonsend.ion.server.features.transport.manager.graph.e2.E2Node
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork.Companion.key
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import org.bukkit.NamespacedKey
import java.util.UUID

class E2GraphManager(manager: TransportHolder) : NetworkManager<E2Node, TransportNetwork<E2Node>>(manager) {
	override val namespacedKey: NamespacedKey = key

	override val cacheFactory = cache
	override fun networkProvider(): E2Network {
		return E2Network(UUID.randomUUID(), this)
	}

	private companion object {
		@JvmStatic
		val cache: BlockBasedCacheFactory<E2Node, NetworkManager<E2Node, TransportNetwork<E2Node>>> = BlockBasedCacheFactory.builder<E2Node, NetworkManager<E2Node, TransportNetwork<E2Node>>>()
			.build()
	}
}
