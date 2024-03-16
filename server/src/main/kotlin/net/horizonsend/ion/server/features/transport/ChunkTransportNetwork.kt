package net.horizonsend.ion.server.features.transport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.grid.PowerGrid
import net.horizonsend.ion.server.features.world.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Chunk
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class ChunkTransportNetwork(
	val chunk: IonChunk,
) {
	// Each chunk gets a scope for parallelism
	private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	val extractorData = getExtractorData(chunk.inner)

	val poweredMultiblockEntities = ConcurrentHashMap<Long, PoweredMultiblockEntity>()
	val powerGrid = PowerGrid(extractorData)

	init {
	    setup()
	}

	private fun setup() {
		collectPowerMultiblockEntities()
	}

	fun tick() {
		for ((location, extractor) in extractorData.extractorLocations) {

		}
	}

	fun save() {

	}

	private fun tickExtractors() {

	}

	private fun collectPowerMultiblockEntities() {
		chunk.multiblockManager.getAllMultiblockEntities().forEach { (key, entity) ->
			if (entity !is PoweredMultiblockEntity) return@forEach

			poweredMultiblockEntities[key] = entity
		}
	}

	private fun getExtractorData(chunk: Chunk): ExtractorData {
		val extractors = chunk.persistentDataContainer.get(NamespacedKeys.EXTRACTOR_DATA, ExtractorData)

		return extractors ?: ExtractorData(ConcurrentLinkedQueue())
	}
}
