package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.filters.manager.ChunkFilterCache
import net.horizonsend.ion.server.features.transport.filters.manager.FilterCache
import net.horizonsend.ion.server.features.transport.inputs.IOManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ChunkExtractorManager
import net.horizonsend.ion.server.features.transport.manager.graph.FluidNetworkManager
import net.horizonsend.ion.server.features.transport.manager.holders.ChunkCacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.World
import org.bukkit.persistence.PersistentDataContainer
import java.util.function.Consumer
import java.util.UUID

class ChunkTransportManager(val chunk: IonChunk) : TransportManager<ChunkCacheHolder<*>>() {
	override val extractorManager: ChunkExtractorManager = ChunkExtractorManager(this)
	override val filterCache: FilterCache = ChunkFilterCache(this)

	override val powerNodeManager = ChunkCacheHolder(this) { PowerTransportCache(it) }
	override val solarPanelManager = ChunkCacheHolder(this) { SolarPanelCache(it) }
	override val itemPipeManager = ChunkCacheHolder(this) { ItemTransportCache(it) }
//	override val fluidNodeManager = ChunkCacheHolder(this) { FluidTransportCache(it) }

	override val cacheHolders: Array<ChunkCacheHolder<*>> = arrayOf(
		powerNodeManager,
		solarPanelManager,
		itemPipeManager,
//		fluidNodeManager
	)

	override val tickedHolders: Array<ChunkCacheHolder<*>> = arrayOf(
		powerNodeManager,
		itemPipeManager,
		solarPanelManager,
//		fluidNodeManager
	)

	override fun getWorld(): World {
		return chunk.world
	}

	override fun getInputProvider(): IOManager {
		return chunk.world.ion.inputManager
	}

	init {
	    extractorManager.onLoad()
	}

	fun setup() {
		cacheHolders.forEach { it.handleLoad() }
		markReady()
		NewTransport.registerTransportManager(this)
	}

	fun onUnload() {
		cacheHolders.forEach { it.handleUnload() }
		NewTransport.removeTransportManager(this)
	}

	fun invalidateCache(x: Int, y: Int, z: Int, player: UUID?) {
		invalidateCache(toBlockKey(x, y, z), player)
	}

	fun invalidateCache(key: BlockKey, player: UUID?) {
		cacheHolders.forEach { it.cache.invalidate(key, player) }
	}

	fun invalidatePathing(x: Int, y: Int, z: Int) {
		invalidatePathing(toBlockKey(x, y, z))
	}

	fun invalidatePathing(key: BlockKey) {
		cacheHolders.forEach { it.cache.invalidateSurroundingPaths(key) }
	}

	override fun storePersistentData(storeConsumer: Consumer<PersistentDataContainer>) {
		storeConsumer.accept(chunk.inner.persistentDataContainer)
	}

	override fun getGraphTransportManager(): FluidNetworkManager {
		return getWorld().ion.transportManager.fluidGraphManager
	}
}
