package net.horizonsend.ion.server.features.transport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot.Companion.snapshot
import net.horizonsend.ion.server.features.transport.grid.PowerGrid
import net.horizonsend.ion.server.features.world.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Chunk
import org.bukkit.event.block.BlockBreakEvent
import java.util.concurrent.ConcurrentLinkedQueue

class ChunkTransportNetwork(
	val chunk: IonChunk,
) {
	// Each chunk gets a scope for parallelism
	val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	val extractorData = getExtractorData(chunk.inner)

	val powerGrid = PowerGrid(this)
	val pipeGrid = PowerGrid(this) // TODO
	val gasGrid = PowerGrid(this) // TODO

	init {
	    setup()
	}

	private fun setup() {
		powerGrid.setup()
		pipeGrid.setup()
		gasGrid.setup()
	}

	fun tick() {
		scope.launch { powerGrid.tick() }
		scope.launch { pipeGrid.tick() }
		scope.launch { gasGrid.tick() }
	}

	fun save() {

	}

	fun processBlockChange(event: BlockBreakEvent) {
		val block = event.block

		val key = toBlockKey(block.x, block.y, block.z)
		val snapshot = block.snapshot()

		processBlockChange(key, snapshot)
	}

	fun processBlockChange(key: Long, new: BlockSnapshot) {
		powerGrid.processBlockChange(key, new)
		pipeGrid.processBlockChange(key, new)
		gasGrid.processBlockChange(key, new)
	}

	private fun getExtractorData(chunk: Chunk): ExtractorData {
		val extractors = chunk.persistentDataContainer.get(NamespacedKeys.EXTRACTOR_DATA, ExtractorData)

		return extractors ?: ExtractorData(ConcurrentLinkedQueue())
	}
}
