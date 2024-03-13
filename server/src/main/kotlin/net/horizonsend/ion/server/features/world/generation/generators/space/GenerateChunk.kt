package net.horizonsend.ion.server.features.world.generation.generators.space

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks
import net.horizonsend.ion.server.features.world.generation.generators.space.GenerateAsteroid.generateAsteroidSection
import net.horizonsend.ion.server.features.world.generation.generators.space.GenerateWreck.generateWreckSection
import net.horizonsend.ion.server.features.world.generation.generators.space.GenerateWreck.getCoveredSections
import net.minecraft.world.level.chunk.LevelChunk

class GenerateChunk(
	val generator: SpaceGenerator,
	val chunk: LevelChunk,
	val wrecks: List<WreckGenerationData>,
	val asteroids: List<AsteroidGenerationData>
) {
	val config = generator.configuration
	var isCancelled = false

	private val chunkMinX = chunk.pos.x.shl(4)
	private val chunkMinZ = chunk.pos.z.shl(4)

	val returnData = CompletableDeferred<StoredChunkBlocks>()

	suspend fun generateChunk(scope: CoroutineScope) {
		val asteroidSectionsMapped = asteroids.associateWith {
			it.getCoveredSections(generator.world.minHeight, generator.world.maxHeight)
		}

		val wreckGenerationDataMap = wrecks.associateWith { it.worldGenData(generator) }

		val wreckSectionsMapped = wreckGenerationDataMap
			.mapValues { (_, data) -> data.region.getCoveredSections(generator.world.minHeight, generator.world.maxHeight) }

		val coveredSections = mutableListOf<IntRange>().apply {
			addAll(asteroidSectionsMapped.values)
			addAll(wreckSectionsMapped.values)
		}

		val sectionRange = IntRange(
			coveredSections.minOf { it.first },
			coveredSections.maxOf { it.last }
		)

		val completableSectionMap: Map<Int, CompletableDeferred<CompletedSection?>> = sectionRange.associateWith { CompletableDeferred() }

		for ((sectionY, deferred) in completableSectionMap) generateSection(
			scope,
			deferred,
			sectionY,
			asteroidSectionsMapped,
			wreckSectionsMapped,
			wreckGenerationDataMap
		)

		val completedSections : List<CompletedSection> = completableSectionMap.mapNotNull { (_, section) ->
			section.await()
		}

		returnData.complete(StoredChunkBlocks(completedSections))
	}

	private fun generateSection(
		scope: CoroutineScope,
		deferred: CompletableDeferred<CompletedSection?>,
		sectionY: Int,
		asteroidSectionsMapped: Map<AsteroidGenerationData, IntRange>,
		wreckSectionsMapped: Map<WreckGenerationData, IntRange>,
		wreckGenerationDataMap: Map<WreckGenerationData, WreckGenerationData.WreckGen>
	) = scope.launch {
		val section = CompletedSection.empty(sectionY)

		for (asteroid in asteroids) {
			if (!asteroidSectionsMapped[asteroid]!!.contains(sectionY)) continue

			generateAsteroidSection(
				generator,
				asteroid,
				section,
				sectionY,
				chunkMinX,
				chunkMinZ,
				asteroid.sizeFactor,
				asteroid.random
			)
		}

		for (wreck in wrecks) {
			if (!wreckSectionsMapped[wreck]!!.contains(sectionY)) continue

			val genData = wreckGenerationDataMap[wreck]!!

			generateWreckSection(
				section,
				genData.clipboard,
				genData.offset,
				sectionY,
				chunkMinX,
				chunkMinZ,
				genData.encounter
			)
		}

		deferred.complete(section)
	}
}
