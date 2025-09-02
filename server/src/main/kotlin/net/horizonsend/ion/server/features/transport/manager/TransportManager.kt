package net.horizonsend.ion.server.features.transport.manager

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.transport.filters.manager.FilterCache
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.AdvancedExtractorData
import net.horizonsend.ion.server.features.transport.manager.graph.E2GraphManager
import net.horizonsend.ion.server.features.transport.manager.graph.FluidNetworkManager
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache

abstract class TransportManager<T: CacheHolder<*>> : TransportHolder {
	abstract val extractorManager: ExtractorManager
	abstract val filterCache: FilterCache

	abstract val powerNodeManager: CacheHolder<PowerTransportCache>
	abstract val solarPanelManager: CacheHolder<SolarPanelCache>
	abstract val itemPipeManager: CacheHolder<ItemTransportCache>

	abstract val cacheHolders: Array<T>
	abstract val tickedHolders: Array<T>

	abstract fun getFluidGraphTransportManager(): FluidNetworkManager
	abstract fun getE2GraphTransportManager(): E2GraphManager

	fun markReady() {
		for (cacheHolder in cacheHolders) {
			cacheHolder.markReady()
		}
	}

	var tickNumber = 0; protected set

	override fun tickExtractors() {
		tickNumber++

		val invalid = LongOpenHashSet()

		val extractors = extractorManager.getExtractors()
		val extractorCount = extractors.size

		for ((index, extractor) in extractors.withIndex()) {
			if (!extractorManager.verifyExtractor(getWorld(), extractor.pos)) {
				invalid.add(extractor.pos)
				continue
			}

			val delta = extractor.markTicked()

			for (network in tickedHolders) {
				network.cache.tickExtractor(extractor.pos, delta, (extractor as? AdvancedExtractorData<*>)?.metaData, index, extractorCount)
			}
		}

		solarPanelManager.cache.tickSolarPanels()

		invalid.forEach(extractorManager::removeExtractor)
	}

	override fun tickGraphs() {}
}
