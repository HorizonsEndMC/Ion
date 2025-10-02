package net.horizonsend.ion.server.features.world.generation.generators

import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.generators.configuration.generator.GenerationConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.chunk.status.ChunkStatus
import org.bukkit.Chunk

abstract class IonWorldGenerator<T: GenerationConfiguration>(val world: IonWorld, val configuration: T) {
	val generationMetaDataFolder = world.world.worldFolder.resolve("data/ion/generation_metadata/${world.world.name}").apply { mkdirs() }
	val seed = world.world.seed
	val heightAccessor: LevelHeightAccessor = LevelHeightAccessor.create(world.world.minHeight, world.world.maxHeight - world.world.minHeight)

	abstract suspend fun generateChunk(chunk: Chunk)

	@Synchronized
	fun addReference(toChunk: ChunkPos, feature: GeneratedFeature<*>, orignChunk: ChunkPos) {
		if (toChunk.getChessboardDistance(orignChunk) > 8) return
		Tasks.sync {
			val worldGenChunk = world.world.minecraft.getChunk(toChunk.x, toChunk.z, ChunkStatus.STRUCTURE_REFERENCES)
			worldGenChunk.addReferenceForStructure(feature.ionStructure.value(), orignChunk.toLong())
		}
	}
}
