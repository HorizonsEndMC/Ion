package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.network.PowerNetwork
import net.horizonsend.ion.server.features.transport.network.holders.ChunkNetworkHolder
import net.horizonsend.ion.server.features.world.chunk.ChunkRegion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class ChunkTransportManager(
	val chunk: IonChunk,
) {
	val scope = ChunkRegion.scope
	val powerNetwork = ChunkNetworkHolder(this) { PowerNetwork(it) }
//	val pipeGrid = PowerNetwork(this) // TODO
//	val gasGrid = PowerNetwork(this) // TODO

	fun setup() {
		powerNetwork.handleLoad()
		// TODO
		// TODO
	}

	suspend fun tick() {
		powerNetwork.network.tickIfReady()
		// TODO
		// TODO
	}

	fun onUnload() {
		powerNetwork.handleUnload()
		// TODO
		// TODO
	}

	fun save() {
		powerNetwork.save(chunk.inner.persistentDataContainer.adapterContext)
		// TODO
		// TODO
	}

	fun processBlockRemoval(key: BlockKey) {
		powerNetwork.network.processBlockRemoval(key)
		// TODO
		// TODO
//		pipeGrid.processBlockRemoval(key)
//		gasGrid.processBlockRemoval(key)
	}

	fun processBlockRemovals(keys: Iterable<BlockKey>) {
		powerNetwork.network.processBlockRemovals(keys)
	}

	fun processBlockAddition(new: BlockSnapshot) {
		powerNetwork.network.processBlockAddition(new)
		// TODO
		// TODO
//		pipeGrid.processBlockAddition(key, new)
//		gasGrid.processBlockAddition(key, new)
	}

	fun processBlockAddition(changes: Iterable<BlockSnapshot>) {
		powerNetwork.network.processBlockAdditions(changes)
	}
}
