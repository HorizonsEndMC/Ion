package net.horizonsend.ion.server.features.transport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot.Companion.snapshot
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.world.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.IntervalExecutor
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Chunk
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import java.util.concurrent.ConcurrentLinkedQueue

class ChunkTransportManager(
	val chunk: IonChunk,
) {
	// Each chunk gets a scope for parallelism
	val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	val extractorData = getExtractorData(chunk.inner)

	val powerNetwork = ChunkPowerNetwork(this)
//	val pipeGrid = ChunkPowerNetwork(this) // TODO
//	val gasGrid = ChunkPowerNetwork(this) // TODO

	fun setup() {
		powerNetwork.setup()
//		pipeGrid.setup()
//		gasGrid.setup()
	}

	val tickExecutor = IntervalExecutor(5) { scope.launch {
		powerNetwork.tick()
	}}

	fun tick() {
		tickExecutor()
	}

	fun save() {
		powerNetwork.save(chunk.inner.persistentDataContainer.adapterContext)
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
//		pipeGrid.processBlockRemoval(key)
//		gasGrid.processBlockRemoval(key)
	}

	fun processBlockAddition(key: Long, new: BlockSnapshot) {
		powerNetwork.processBlockAddition(key, new)
//		pipeGrid.processBlockAddition(key, new)
//		gasGrid.processBlockAddition(key, new)
	}

	private fun getExtractorData(chunk: Chunk): ExtractorData {
		val extractors = chunk.persistentDataContainer.get(NamespacedKeys.EXTRACTOR_DATA, ExtractorData)

		return extractors ?: ExtractorData(ConcurrentLinkedQueue())
	}
}
