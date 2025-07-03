package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.filters.manager.ShipFilterCache
import net.horizonsend.ion.server.features.transport.inputs.IOManager
import net.horizonsend.ion.server.features.transport.inputs.ShipIOManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ShipExtractorManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.AdvancedExtractorData
import net.horizonsend.ion.server.features.transport.manager.graph.FluidNetworkManager
import net.horizonsend.ion.server.features.transport.manager.holders.ShipCacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World

class ShipTransportManager(val starship: Starship) : TransportManager<ShipCacheHolder<*>>() {
	override val extractorManager: ShipExtractorManager = ShipExtractorManager(this)
	override val filterCache: ShipFilterCache = ShipFilterCache(this)
	val ioManager = ShipIOManager(this)

	override fun getWorld(): World = starship.world

	override val powerNodeManager = ShipCacheHolder(this) { PowerTransportCache(it) }
	override val solarPanelManager = ShipCacheHolder(this) { SolarPanelCache(it) }
	override val itemPipeManager = ShipCacheHolder(this) { ItemTransportCache(it) }
//	override val fluidNodeManager = ShipCacheHolder(this) { FluidTransportCache(it) }

	override val cacheHolders: Array<ShipCacheHolder<*>> = arrayOf(
		powerNodeManager,
		solarPanelManager,
		itemPipeManager,
//		fluidNodeManager
	)

	override val tickedHolders: Array<ShipCacheHolder<*>> = arrayOf(
		powerNodeManager,
		itemPipeManager,
		solarPanelManager,
//		fluidNodeManager
	)

	fun processLoad() {
		cacheHolders.forEach { it.handleLoad() }
		filterCache
		extractorManager.loadExtractors()
		NewTransport.registerTransportManager(this)
	}

	fun onDestroy() {
		cacheHolders.forEach { it.release() }
		NewTransport.removeTransportManager(this)
		extractorManager.releaseExtractors()
	}

	fun displace(movement: StarshipMovement) {
		cacheHolders.forEach { it.displace(movement) }
	}

	override fun getInputProvider(): IOManager {
		return ioManager
	}

	override fun getGlobalCoordinate(localVec3i: Vec3i): Vec3i {
		return starship.getGlobalCoordinate(localVec3i)
	}

	override fun getLocalCoordinate(globalVec3i: Vec3i): Vec3i {
		return starship.getLocalCoordinate(globalVec3i)
	}

	fun clearData() {
		extractorManager.extractors.clear()
		filterCache.filters.clear()
	}

	override fun tick() {
		tickNumber++

		val extractors = extractorManager.getExtractors()
		val extractorCount = extractors.size

		for ((index, extractor) in extractors.withIndex()) {
			if (!starship.isTeleporting && !extractorManager.verifyExtractor(getWorld(), extractor.pos)) {
				continue
			}

			val delta = extractor.markTicked()

			for (network in tickedHolders) {
				network.cache.tickExtractor(extractor.pos, delta, (extractor as? AdvancedExtractorData<*>)?.metaData, index, extractorCount)
			}
		}

		solarPanelManager.cache.tickSolarPanels()
	}

	override fun getGraphTransportManager(): FluidNetworkManager {
		TODO()
//		return getWorld().ion.transportManager.fluidGraphManager
	}
}
