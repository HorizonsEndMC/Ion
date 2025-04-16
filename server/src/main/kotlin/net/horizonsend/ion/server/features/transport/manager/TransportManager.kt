package net.horizonsend.ion.server.features.transport.manager

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.transport.filters.manager.FilterCache
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.AdvancedExtractorData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World

abstract class TransportManager<T: CacheHolder<*>> {
	abstract val extractorManager: ExtractorManager
	abstract val filterCache: FilterCache

	abstract val powerNodeManager: CacheHolder<PowerTransportCache>
	abstract val solarPanelManager: CacheHolder<SolarPanelCache>
	abstract val itemPipeManager: CacheHolder<ItemTransportCache>
//	abstract val fluidNodeManager: CacheHolder<FluidTransportCache>

	abstract val cacheHolders: Array<T>
	abstract val tickedHolders: Array<T>

	fun markReady() {
		for (cacheHolder in cacheHolders) {
			cacheHolder.markReady()
		}
	}

	abstract fun getInputProvider(): InputManager

	open fun tick() {
		val invalid = LongOpenHashSet()

		for (extractor in extractorManager.getExtractors()) {
			if (!extractorManager.verifyExtractor(getWorld(), extractor.pos)) {
				invalid.add(extractor.pos)
				continue
			}

			val delta = extractor.markTicked()

			for (network in tickedHolders) {
				network.cache.tickExtractor(extractor.pos, delta, (extractor as? AdvancedExtractorData<*>)?.metaData)
			}
		}

		invalid.forEach(extractorManager::removeExtractor)
	}

	abstract fun getWorld(): World

	open fun getGlobalCoordinate(localVec3i: Vec3i): Vec3i = localVec3i
	open fun getLocalCoordinate(globalVec3i: Vec3i): Vec3i = globalVec3i
}
