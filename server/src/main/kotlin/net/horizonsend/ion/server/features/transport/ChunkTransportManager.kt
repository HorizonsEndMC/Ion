package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.manager.holders.ChunkNetworkHolder
import net.horizonsend.ion.server.features.world.chunk.ChunkRegion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.Block

class ChunkTransportManager(
	val chunk: IonChunk,
) {
	val scope = ChunkRegion.scope
	val powerNodeManager = ChunkNetworkHolder(this) { PowerNodeManager(it) }
//	val pipeGrid = PowerNodeManager(this) // TODO
//	val gasGrid = PowerNodeManager(this) // TODO

	fun setup() {
		powerNodeManager.handleLoad()
		// TODO
		// TODO
	}

	fun onUnload() {
		powerNodeManager.handleUnload()
		// TODO
		// TODO
	}

	fun save() {
		powerNodeManager.save(chunk.inner.persistentDataContainer.adapterContext)
		// TODO
		// TODO
	}

	fun processBlockRemoval(key: BlockKey) {
		powerNodeManager.network.processBlockRemoval(key)
		// TODO
		// TODO
//		pipeGrid.processBlockRemoval(key)
//		gasGrid.processBlockRemoval(key)
	}

	fun processBlockRemovals(keys: Iterable<BlockKey>) {
		powerNodeManager.network.processBlockRemovals(keys)
	}

	fun processBlockAddition(new: Block) {
		powerNodeManager.network.processBlockAddition(new)
		// TODO
		// TODO
//		pipeGrid.processBlockAddition(key, new)
//		gasGrid.processBlockAddition(key, new)
	}

	fun processBlockAddition(changed: Iterable<Block>) {
		powerNodeManager.network.processBlockAdditions(changed)
	}
}
