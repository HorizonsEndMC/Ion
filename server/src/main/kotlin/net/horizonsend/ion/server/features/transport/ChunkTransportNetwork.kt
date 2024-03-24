package net.horizonsend.ion.server.features.transport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.transport.grid.PowerGrid
import net.horizonsend.ion.server.features.world.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Chunk
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

	private fun getExtractorData(chunk: Chunk): ExtractorData {
		val extractors = chunk.persistentDataContainer.get(NamespacedKeys.EXTRACTOR_DATA, ExtractorData)

		return extractors ?: ExtractorData(ConcurrentLinkedQueue())
	}
}
