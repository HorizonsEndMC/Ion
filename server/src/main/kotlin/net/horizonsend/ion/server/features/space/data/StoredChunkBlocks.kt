package net.horizonsend.ion.server.features.space.data

import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.Chunk
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

data class StoredChunkBlocks(val sections: List<CompletedSection>) {
	companion object : PersistentDataType<PersistentDataContainer, StoredChunkBlocks> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType() = StoredChunkBlocks::class.java

		override fun toPrimitive(complex: StoredChunkBlocks, context: PersistentDataAdapterContext): PersistentDataContainer {
			val primitive = context.newPersistentDataContainer()

			val paletteArray = complex.sections
				.map { CompletedSection.toPrimitive(it, context) }
				.toTypedArray()

			primitive.set(NamespacedKeys.SECTIONS, PersistentDataType.TAG_CONTAINER_ARRAY, paletteArray)

			return primitive
		}

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): StoredChunkBlocks {
			val sections = primitive.get(
				NamespacedKeys.SECTIONS, PersistentDataType.TAG_CONTAINER_ARRAY
			)!!.map { CompletedSection.fromPrimitive(it, context) }

			return StoredChunkBlocks(sections)
		}

		fun StoredChunkBlocks.store(chunk: Chunk) {
			chunk.persistentDataContainer.set(NamespacedKeys.STORED_CHUNK_BLOCKS, StoredChunkBlocks, this)
		}

		fun StoredChunkBlocks.place(chunk: Chunk) {
			val levelChunk = chunk.minecraft

			for (section in this.sections) {
				section.place(levelChunk)
			}
		}
	}
}
