package net.horizonsend.ion.server.features.world.generation.generators

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.WorldGenerationManager
import net.horizonsend.ion.server.features.world.generation.feature.AsteroidFeature
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.configuration.FeatureConfiguration
import net.horizonsend.ion.server.features.world.generation.generators.configuration.FeatureGeneratorConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.world.level.ChunkPos
import org.bukkit.Chunk
import kotlin.random.Random

class FeatureGenerator(world: IonWorld, configuration: FeatureGeneratorConfiguration) : IonWorldGenerator<FeatureGeneratorConfiguration>(world, configuration) {
	val features = configuration.features.map(FeatureConfiguration::loadFeature).plus(AsteroidFeature)

	override suspend fun generateChunk(chunk: Chunk) {
//		if (features.isEmpty()) return

		val nmsChunk = chunk.minecraft
		val pos = nmsChunk.pos

		// Search #FEATURE_START_SEARCH_RANGE chunks ahead for structure starts
		getStartSearchChunks(pos).forEach(::buildStructureData)

		// Get data for this chunk
		val data = chunkDataCache[pos]

		val starts = data.starts
		val references = data.references

		val referencedStarts = references.flatMap { (_, referenced) ->
			referenced.flatMap { key -> chunkDataCache[ChunkPos(key)].starts }
		}

		val toGenerate = starts.plus(referencedStarts)
		if (toGenerate.isEmpty()) return

		val sectionsB = toGenerate
			.associateWith { featureStart: FeatureStart ->
				val deferred = CompletableDeferred<List<CompletedSection>>()
				WorldGenerationManager.coroutineScope.launch {
					deferred.complete(featureStart.feature.castAndGenerateChunk(this@FeatureGenerator, pos, featureStart))
				}

				deferred
			}

		sectionsB.values.awaitAll()
		val sections = sectionsB.entries.sortedBy { it.key.feature.placementConfiguration.placementPriority }

		sections.forEach { t ->  t.value.await().forEach { section -> section.place(nmsChunk) } }
		nmsChunk.`moonrise$getChunkAndHolder`().holder.broadcastChanges(nmsChunk)
	}

	fun buildStructureData(originChunk: ChunkPos) {
		val toGenerate = addStructureStarts(originChunk)
		saveStarts(originChunk, toGenerate)

		for (start in toGenerate) {
			val (chunkMin, chunkMax) = start.feature.getChunkExtents(start)

			for (chunkX in chunkMin.x..chunkMax.x) for (chunkZ in chunkMin.z..chunkMax.z) {
				addReference(ChunkPos(chunkX, chunkZ), start.feature, originChunk)
			}
		}
	}

	fun addStructureStarts(chunk: ChunkPos): MutableList<FeatureStart> {
		val starts = mutableListOf<FeatureStart>()
		val chunkRandom = Random(chunk.longKey)

		for (feature in features.filter { feature -> feature.canPlace() }) {
			val featureStarts = feature.buildStartsData(chunk, chunkRandom)
			starts.addAll(featureStarts)
		}

		return starts
	}

	companion object {
		const val FEATURE_START_SEARCH_RANGE = 10

		fun getStartSearchChunks(chunkPos: ChunkPos): List<ChunkPos> {
			val chunks = mutableListOf<ChunkPos>()

			for (x in -FEATURE_START_SEARCH_RANGE..FEATURE_START_SEARCH_RANGE) for (z in -FEATURE_START_SEARCH_RANGE..FEATURE_START_SEARCH_RANGE) {
				chunks.add(ChunkPos(chunkPos.x + x, chunkPos.z + z))
			}

			return chunks
		}
	}
}
