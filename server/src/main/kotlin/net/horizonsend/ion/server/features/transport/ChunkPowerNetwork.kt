package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.features.multiblock.entity.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.world.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Chunk
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class ChunkPowerNetwork(
	val chunk: IonChunk,
) {
	val extractorData = getExtractorData(chunk.inner)

	init {
	    setup()
	}

	private fun setup() {

	}

	fun tick() {

	}

	fun save() {

	}

	private fun getPowerMultiblockEntities(): ConcurrentHashMap<Long, PoweredMultiblockEntity> {
		val poweredMultiblockEntities = ConcurrentHashMap<Long, PoweredMultiblockEntity>()

		chunk.multiblockManager.getAllMultiblockEntities().forEach { (key, entity) ->
			if (entity !is PoweredMultiblockEntity) return@forEach

			poweredMultiblockEntities[key] = entity
		}

		return poweredMultiblockEntities
	}

	fun getExtractorData(chunk: Chunk): ExtractorData {
		val extractors = chunk.persistentDataContainer.get(NamespacedKeys.EXTRACTOR_DATA, ExtractorData)

		return extractors ?: ExtractorData(ConcurrentLinkedQueue())
	}
}
