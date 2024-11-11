package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.transport.nodes.cache.FluidTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.manager.holders.NetworkHolder

abstract class TransportManager {
	abstract val extractorManager: ExtractorManager
	abstract val powerNodeManager: NetworkHolder<PowerTransportCache>
	abstract val fluidNodeManager: NetworkHolder<FluidTransportCache>

	fun tick() {

	}
}
