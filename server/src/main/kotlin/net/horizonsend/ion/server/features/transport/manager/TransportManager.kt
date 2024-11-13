package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.FluidTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager

abstract class TransportManager {
	abstract val extractorManager: ExtractorManager
	abstract val powerNodeManager: NetworkHolder<PowerTransportCache>
	abstract val fluidNodeManager: NetworkHolder<FluidTransportCache>

	abstract fun getInputProvider(): InputManager

	fun tick() {
		for (extractor in extractorManager.getExtractors()) {
			val delta = extractor.markTicked()
			powerNodeManager.network.tickExtractor(extractor.pos, delta)
			fluidNodeManager.network.tickExtractor(extractor.pos, delta)
		}
	}
}
