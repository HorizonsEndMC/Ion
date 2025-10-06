package net.horizonsend.ion.server.features.world.generation.generators

import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.feature.nms.NMSStructureIntegration
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.configuration.feature.AsteroidPlacementConfiguration
import net.horizonsend.ion.server.features.world.generation.generators.configuration.generator.FeatureGeneratorConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.server.level.GenerationChunkHolder
import net.minecraft.util.StaticCache2D
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.level.chunk.status.ChunkStep
import net.minecraft.world.level.chunk.status.WorldGenContext
import org.bukkit.Bukkit
import org.bukkit.generator.WorldInfo
import kotlin.random.Random

class FeatureGenerator(world: IonWorld, configuration: FeatureGeneratorConfiguration) : IonWorldGenerator<FeatureGeneratorConfiguration>(world, configuration) {
	val features = configuration.features.plus(AsteroidPlacementConfiguration())

	override fun generateStructureStarts(context: WorldGenContext, step: ChunkStep, neighborCache: StaticCache2D<GenerationChunkHolder>, chunk: ChunkAccess) {
		val toGenerate = generateStructureStarts(chunk.pos)

		for (start in toGenerate) {
			chunk.setStartForStructure(start.feature.ionStructure.value(), start.getNMS())
		}
	}

	override fun generateNoise(worldInfo: WorldInfo, random: java.util.Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
		val level = Bukkit.getWorld(worldInfo.uid)!!.minecraft
		val nmsChunk = level.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_STARTS)

		val pos = ChunkPos(chunkX, chunkZ)

		// Get data for this chunk
		val referencedStarts = mutableListOf<FeatureStart>()

		val starts = nmsChunk.allStarts
		for ((structure, nmsStart) in starts) {
			if (structure !is NMSStructureIntegration.IonStructure) continue
			referencedStarts.add(FeatureStart.fromNMS(nmsStart))
		}

		val references = nmsChunk.allReferences
		for ((structure, chunkReferences) in references) {
			if (structure !is NMSStructureIntegration.IonStructure) continue

			for (key in chunkReferences.iterator()) {
				val referencePos = ChunkPos(key)
				val referencedChunk = level.getChunk(referencePos.x, referencePos.z, ChunkStatus.STRUCTURE_STARTS)
				val nmsStart = referencedChunk.getStartForStructure(structure) ?: continue
				referencedStarts.add(FeatureStart.fromNMS(nmsStart))
			}
		}

		if (referencedStarts.isEmpty()) return

		for (start in referencedStarts) {
			start.feature.castAndGenerateChunk(this, pos, chunkData, start)
		}
	}

	companion object {
		// NMS starts throwing warnings if it goes beyond 8
		const val FEATURE_START_SEARCH_RANGE = 8

		fun getStartSearchChunks(chunkPos: ChunkPos): List<ChunkPos> {
			val chunks = mutableListOf<ChunkPos>()

			for (x in -FEATURE_START_SEARCH_RANGE..FEATURE_START_SEARCH_RANGE) for (z in -FEATURE_START_SEARCH_RANGE..FEATURE_START_SEARCH_RANGE) {
				chunks.add(ChunkPos(chunkPos.x + x, chunkPos.z + z))
			}

			return chunks
		}
	}

	/**
	 * Generates the structures that have their origin in this chunk
	 **/
	private fun generateStructureStarts(chunk: ChunkPos): List<FeatureStart> {
		val starts = mutableListOf<FeatureStart>()
		val chunkRandom = Random(chunk.longKey)

		for (feature in features.filter { feature -> feature.getFeatureKey().getValue().canPlace() }) {
			val featureStarts = feature.buildStartsData(world.world, chunk, chunkRandom)

			starts.addAll(featureStarts)
		}

		return starts
	}
}
