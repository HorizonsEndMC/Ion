package net.horizonsend.ion.server.features.transport.cache

import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.util.NetworkType

class FluidTransportCache(holder: NetworkHolder<FluidTransportCache>): TransportCache(holder) {
	override val type: NetworkType = NetworkType.FLUID
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.build()
}
