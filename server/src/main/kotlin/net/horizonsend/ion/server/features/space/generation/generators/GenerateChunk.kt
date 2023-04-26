package net.horizonsend.ion.server.features.space.generation.generators

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks
import net.horizonsend.ion.server.features.space.generation.generators.GenerateAsteroid.generateAsteroidSection
import net.horizonsend.ion.server.features.space.generation.generators.GenerateWreck.generateWreckSection
import net.horizonsend.ion.server.features.space.generation.generators.GenerateWreck.getCoveredSections
import net.minecraft.world.level.chunk.LevelChunk

class GenerateChunk(
	val generator: SpaceGenerator,
	val chunk: LevelChunk,
	val wrecks: List<WreckGenerationData>,
	val asteroids: List<AsteroidGenerationData>
) {
	val config = generator.configuration
	var isCancelled = false

	val returnData = CompletableDeferred<StoredChunkBlocks>()

	suspend fun generateChunk(scope: CoroutineScope) {
		val completableSectionMap = mutableMapOf<Int, CompletableDeferred<CompletedSection?>>()
		val wreckGenerationDataMap = wrecks.associateWith { it.worldGenData(generator) }

		val coveredSections = mutableListOf<IntRange>()

		val asteroidSectionsMapped = asteroids.associateWith {
			it.getCoveredSections(generator.serverLevel.minBuildHeight, generator.serverLevel.maxBuildHeight)
		}

		val wreckSectionsMapped = wreckGenerationDataMap.map { (wreck, data) ->
			wreck to data.region.getCoveredSections(generator.serverLevel.minBuildHeight, generator.serverLevel.maxBuildHeight)
		}.toMap()

		coveredSections.addAll(asteroidSectionsMapped.values)
		coveredSections.addAll(wreckSectionsMapped.values)

		val sectionRange = IntRange(
			coveredSections.minOf { it.first },
			coveredSections.maxOf { it.last }
		)

		for (sectionY in sectionRange) {
			completableSectionMap[sectionY] = CompletableDeferred()
		}

		val chunkMinX = chunk.pos.x.shl(4)
		val chunkMinZ = chunk.pos.z.shl(4)

		for ((sectionY, deferred) in completableSectionMap) {
			scope.launch {
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

		complete(completableSectionMap)
	}

	private suspend fun complete(sections: Map<Int, CompletableDeferred<CompletedSection?>>) {
		val completedSections = mutableMapOf<Int, CompletedSection>()

		sections.values.awaitAll()

		for ((y, section) in sections) {
			completedSections[y] = section.await() ?: continue
		}

		returnData.complete(StoredChunkBlocks(completedSections.values.toList()))
	}
}
