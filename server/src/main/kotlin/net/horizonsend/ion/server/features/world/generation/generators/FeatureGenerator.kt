package net.horizonsend.ion.server.features.world.generation.generators

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.WorldGenerationManager
import net.horizonsend.ion.server.features.world.generation.feature.nms.IonStructureTypes
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.configuration.AsteroidPlacementConfiguration
import net.horizonsend.ion.server.features.world.generation.generators.configuration.FeatureGeneratorConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.status.ChunkStatus
import org.bukkit.Chunk
import kotlin.random.Random

class FeatureGenerator(world: IonWorld, configuration: FeatureGeneratorConfiguration) : IonWorldGenerator<FeatureGeneratorConfiguration>(world, configuration) {
	val features = configuration.features.plus(AsteroidPlacementConfiguration())

	override suspend fun generateChunk(chunk: Chunk) {
//		if (features.isEmpty()) return

		val nmsChunk = chunk.minecraft
		val pos = nmsChunk.pos

		// Search #FEATURE_START_SEARCH_RANGE chunks ahead for structure starts
		getStartSearchChunks(pos).forEach(::buildStructureData)

		// Get data for this chunk

		val referencedStarts = mutableListOf<FeatureStart>()

		val starts = nmsChunk.allStarts
		for ((structure, nmsStart) in starts) {
			if (structure !is IonStructureTypes.IonStructure) continue
			referencedStarts.add(FeatureStart.fromNMS(nmsStart))
		}

		val references = nmsChunk.allReferences
		for ((structure, chunkReferences) in references) {
			if (structure !is IonStructureTypes.IonStructure) continue

			for (key in chunkReferences.iterator()) {
				val referencePos = ChunkPos(key)
				val referencedChunk = nmsChunk.level.getChunk(referencePos.x, referencePos.z, ChunkStatus.STRUCTURE_STARTS)
				val nmsStart = referencedChunk.getStartForStructure(structure) ?: continue
				referencedStarts.add(FeatureStart.fromNMS(nmsStart))
			}
		}

		if (referencedStarts.isEmpty()) return

		val sectionsB = referencedStarts
			.associateWith { featureStart: FeatureStart ->
				val deferred = CompletableDeferred<List<CompletedSection>>()
				WorldGenerationManager.coroutineScope.launch {
					deferred.complete(featureStart.feature.castAndGenerateChunk(this@FeatureGenerator, pos, featureStart))
				}

				deferred
			}

		sectionsB.values.awaitAll()
		val sections = sectionsB.entries.sortedBy { it.key.feature.placementPriority }

		sections.forEach { t -> t.value.await().forEach { section -> section.place(nmsChunk) } }
		nmsChunk.`moonrise$getChunkAndHolder`().holder.broadcastChanges(nmsChunk)
	}

	fun buildStructureData(originChunk: ChunkPos) {
		val toGenerate = addStructureStarts(originChunk)

		val worldGenChunk = world.world.minecraft.getChunk(originChunk.x, originChunk.z, ChunkStatus.STRUCTURE_STARTS)
		for (start in toGenerate) {
			worldGenChunk.setStartForStructure(start.feature.ionStructure.value(), start.getNMS())
		}

		for (start in toGenerate) {
			val (chunkMin, chunkMax) = start.feature.getChunkExtents(start)

			for (chunkX in chunkMin.x..chunkMax.x) for (chunkZ in chunkMin.z..chunkMax.z) {
				addReference(ChunkPos(chunkX, chunkZ), start.feature, originChunk)
			}
		}
	}

	private fun addStructureStarts(chunk: ChunkPos): MutableList<FeatureStart> {
		val starts = mutableListOf<FeatureStart>()
		val chunkRandom = Random(chunk.longKey)

		for (feature in features.filter { feature -> feature.getFeature().canPlace() }) {
			val featureStarts = feature.buildStartsData(world.world, chunk, chunkRandom)

			starts.addAll(featureStarts)
		}

		return starts
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
}
