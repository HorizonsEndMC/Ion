package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot.Companion.snapshot
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.world.chunk.ChunkRegion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

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

	fun processBlockRemoval(event: BlockBreakEvent) {
		val block = event.block
		val key = toBlockKey(block.x, block.y, block.z)

		processBlockRemoval(key)
	}

	fun processBlockAddition(event: BlockPlaceEvent) {
		val block = event.block

		val key = toBlockKey(block.x, block.y, block.z)
		val snapshot = block.snapshot()

		processBlockAddition(key, snapshot)
	}

	fun processBlockRemoval(key: Long) {
		powerNetwork.processBlockRemoval(key)
		// TODO
		// TODO
//		pipeGrid.processBlockRemoval(key)
//		gasGrid.processBlockRemoval(key)
	}

	fun processBlockAddition(key: Long, new: BlockSnapshot) {
		powerNetwork.processBlockAddition(key, new)
		// TODO
		// TODO
//		pipeGrid.processBlockAddition(key, new)
//		gasGrid.processBlockAddition(key, new)
	}
}
