package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.world.chunk.ChunkRegion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class ChunkTransportManager(
	val chunk: IonChunk,
) {
	val scope = ChunkRegion.scope
	val powerNetwork = ChunkPowerNetwork(this)
//	val pipeGrid = ChunkPowerNetwork(this) // TODO
//	val gasGrid = ChunkPowerNetwork(this) // TODO

	fun setup() {
		powerNetwork.loadNetwork()
		// TODO
		// TODO
	}

	suspend fun tick() {
		powerNetwork.tickIfReady()
		// TODO
		// TODO
	}

	fun onUnload() {
		powerNetwork.onUnload()
		// TODO
		// TODO
	}

	fun save() {
		powerNetwork.save(chunk.inner.persistentDataContainer.adapterContext)
		// TODO
		// TODO
	}

	fun processBlockRemoval(key: BlockKey) {
		powerNetwork.processBlockRemoval(key)
		// TODO
		// TODO
//		pipeGrid.processBlockRemoval(key)
//		gasGrid.processBlockRemoval(key)
	}

	fun processBlockRemovals(keys: Iterable<BlockKey>) {
		powerNetwork.processBlockRemovals(keys)
	}

	fun processBlockAddition(new: BlockSnapshot) {
		powerNetwork.processBlockAddition(new)
		// TODO
		// TODO
//		pipeGrid.processBlockAddition(key, new)
//		gasGrid.processBlockAddition(key, new)
	}

	fun processBlockAddition(changes: Iterable<BlockSnapshot>) {
		powerNetwork.processBlockAdditions(changes)
	}
}
