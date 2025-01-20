package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.extractors.ChunkExtractorManager
import net.horizonsend.ion.server.features.transport.manager.holders.ChunkCacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.block.Block

class ChunkTransportManager(val chunk: IonChunk) : TransportManager<ChunkCacheHolder<*>>() {
	override val extractorManager: ChunkExtractorManager = ChunkExtractorManager(this)

	override val powerNodeManager = ChunkCacheHolder(this) { PowerTransportCache(it) }
	override val solarPanelManager = ChunkCacheHolder(this) { SolarPanelCache(it) }
	override val itemPipeManager = ChunkCacheHolder(this) { ItemTransportCache(it) }
//	override val fluidNodeManager = ChunkCacheHolder(this) { FluidTransportCache(it) }

	override val networks: Array<ChunkCacheHolder<*>> = arrayOf(
		powerNodeManager,
		solarPanelManager,
		itemPipeManager,
//		fluidNodeManager
	)

	override fun getInputProvider(): InputManager {
		return chunk.world.ion.inputManager
	}

	init {
	    extractorManager.onLoad()
	}

	fun setup() {
		networks.forEach { it.handleLoad() }
		NewTransport.registerTransportManager(this)
	}

	fun onUnload() {
		networks.forEach { it.handleUnload() }
		NewTransport.removeTransportManager(this)
	}

	fun invalidateCache(x: Int, y: Int, z: Int) {
		invalidateCache(toBlockKey(x, y, z))
	}

	fun invalidateCache(key: BlockKey) {
		networks.forEach { it.cache.invalidate(key) }
	}

	fun processBlockRemoval(key: BlockKey) {
		networks.forEach { it.cache.invalidate(key) }
	}

	fun processBlockChange(block: Block) {
		networks.forEach { it.cache.invalidate(toBlockKey(block.x, block.y, block.z)) }
	}
}
