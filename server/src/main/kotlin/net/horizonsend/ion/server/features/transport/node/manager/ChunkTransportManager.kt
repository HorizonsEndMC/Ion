package net.horizonsend.ion.server.features.transport.node.manager

import net.horizonsend.ion.server.features.transport.node.manager.holders.ChunkNetworkHolder
import net.horizonsend.ion.server.features.transport.node.manager.node.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.manager.node.PowerNodeManager
import net.horizonsend.ion.server.features.world.chunk.ChunkRegion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

class ChunkTransportManager(val chunk: IonChunk) : TransportManager() {
	val scope = ChunkRegion.scope

	override val powerNodeManager = ChunkNetworkHolder(this) { PowerNodeManager(it) }
	override val fluidNodeManager = ChunkNetworkHolder(this) { FluidNodeManager(it) }
//	val pipeGrid = PowerNodeManager(this) // TODO

	fun setup() {
		powerNodeManager.handleLoad()
		fluidNodeManager.handleLoad()
	}

	fun onUnload() {
		powerNodeManager.handleUnload()
		fluidNodeManager.handleUnload()
	}

	fun save() {
		powerNodeManager.save(chunk.inner.persistentDataContainer.adapterContext)
		fluidNodeManager.save(chunk.inner.persistentDataContainer.adapterContext)
	}

	fun processBlockRemoval(key: BlockKey) {
		powerNodeManager.network.processBlockRemoval(key)
		fluidNodeManager.network.processBlockRemoval(key)
//		pipeGrid.processBlockRemoval(key)
	}

	fun processBlockRemovals(keys: Iterable<BlockKey>) {
		powerNodeManager.network.processBlockRemovals(keys)
		fluidNodeManager.network.processBlockRemovals(keys)
	}

	fun processBlockChange(new: Block) {
		powerNodeManager.network.processBlockChange(new)
		fluidNodeManager.network.processBlockChange(new)
//		pipeGrid.processBlockAddition(key, new)
	}

	fun processBlockChange(position: BlockKey, data: BlockData) {
		powerNodeManager.network.processBlockChange(position, data)
		fluidNodeManager.network.processBlockChange(position, data)
//		pipeGrid.processBlockAddition(key, new)
	}

	fun processBlockChanges(changeMap: Map<BlockKey, BlockData>) {
		powerNodeManager.network.processBlockChanges(changeMap)
		fluidNodeManager.network.processBlockChanges(changeMap)
	}

	fun refreshBlock(position: BlockKey) {
		powerNodeManager.network.processBlockChange(position)
		fluidNodeManager.network.processBlockChange(position)
//		pipeGrid.processBlockAddition(key, new)
	}

	fun processBlockChange(changed: Iterable<Block>) {
		powerNodeManager.network.processBlockAdditions(changed)
		fluidNodeManager.network.processBlockAdditions(changed)
	}
}
