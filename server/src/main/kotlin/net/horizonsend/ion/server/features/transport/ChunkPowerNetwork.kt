package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.features.multiblock.entity.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.world.IonChunk
import java.util.concurrent.ConcurrentHashMap

class ChunkPowerNetwork(
	val chunk: IonChunk,
	val extractorData: ExtractorData
) {
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

		chunk.getAllMultiblockEntities().forEach { (key, entity) ->
			if (entity !is PoweredMultiblockEntity) return@forEach

			poweredMultiblockEntities[key] = entity
		}

		return poweredMultiblockEntities
	}
}
