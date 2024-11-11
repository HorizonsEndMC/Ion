package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.transport.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.util.NetworkType

class FluidTransportCache(holder: NetworkHolder<FluidTransportCache>): TransportCache(holder) {
	override val type: NetworkType = NetworkType.FLUID
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.build()
}
