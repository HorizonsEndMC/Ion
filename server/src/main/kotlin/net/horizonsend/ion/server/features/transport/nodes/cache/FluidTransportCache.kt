package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.transport.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.util.NetworkType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class FluidTransportCache(holder: NetworkHolder<FluidTransportCache>): TransportCache(holder) {
	override val type: NetworkType = NetworkType.FLUID
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.build()

	override fun tickExtractor(location: BlockKey) {

	}
}
