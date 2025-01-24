package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.transport.filters.manager.FilterManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.AdvancedExtractorData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i

abstract class TransportManager<T: CacheHolder<*>> {
	abstract val extractorManager: ExtractorManager
	abstract val filterManager: FilterManager

	abstract val powerNodeManager: CacheHolder<PowerTransportCache>
	abstract val solarPanelManager: CacheHolder<SolarPanelCache>
	abstract val itemPipeManager: CacheHolder<ItemTransportCache>
//	abstract val fluidNodeManager: CacheHolder<FluidTransportCache>

	abstract val cacheHolders: Array<T>
	abstract val tickedHolders: Array<T>

	abstract fun getInputProvider(): InputManager

	fun tick() {
		for (extractor in extractorManager.getExtractors()) {
			val delta = extractor.markTicked()

			for (network in tickedHolders) {
				network.cache.tickExtractor(extractor.pos, delta, (extractor as? AdvancedExtractorData<*>)?.metaData)
			}
		}
	}

	open fun getGlobalCoordinate(localVec3i: Vec3i): Vec3i = localVec3i
	open fun getLocalCoordinate(globalVec3i: Vec3i): Vec3i = globalVec3i
}
