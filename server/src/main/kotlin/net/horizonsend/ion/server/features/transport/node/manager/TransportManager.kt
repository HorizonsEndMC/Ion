package net.horizonsend.ion.server.features.transport.node.manager

import net.horizonsend.ion.server.features.transport.cache.FluidTransportCache
import net.horizonsend.ion.server.features.transport.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.node.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder

abstract class TransportManager {
	abstract val extractorManager: ExtractorManager
	abstract val powerNodeManager: NetworkHolder<PowerTransportCache>
	abstract val fluidNodeManager: NetworkHolder<FluidTransportCache>

	fun tick() {

	}
}
