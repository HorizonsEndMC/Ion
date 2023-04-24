package net.horizonsend.ion.server.features.space.generation.generators

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.data.BlockData
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks
import net.horizonsend.ion.server.miscellaneous.WeightedRandomList
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.util.noise.PerlinOctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.Random
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class GenerateAsteroidTask(
	override val generator: SpaceGenerator,
	override val chunk: LevelChunk,
	val asteroids: List<AsteroidGenerationData>
) : SpaceGenerationTask() {
	val config = generator.configuration

	override val returnData = CompletableDeferred<StoredChunkBlocks>()

	override suspend fun generateChunk(scope: CoroutineScope) {
		val completableSectionMap = mutableMapOf<AsteroidGenerationData, Map<Int, CompletableDeferred<CompletedSection>>>()

		for (asteroid in asteroids) {
			val shapingNoise = SimplexOctaveGenerator(asteroid.seed, 1)
			val materialNoise = SimplexOctaveGenerator(asteroid.seed, 1)
			val taskRandom = Random(asteroid.seed)

			val sizeFactor = asteroid.size / 15

			val cave1 = PerlinOctaveGenerator(taskRandom.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / asteroid.size))) }
			val cave2 = PerlinOctaveGenerator(taskRandom.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / asteroid.size))) }

			materialNoise.setScale(0.15 / sqrt(sizeFactor))

			val yRange = IntRange(
				(asteroid.y - (asteroid.size * generator.searchRadius).toInt()).coerceAtLeast(generator.serverLevel.minBuildHeight),
				(asteroid.y + (asteroid.size * generator.searchRadius).toInt()).coerceAtMost(generator.serverLevel.maxBuildHeight)
			)

			val completableSections = mutableMapOf<Int, CompletableDeferred<CompletedSection>>()

			for (chunkSectionY in
				yRange.first.shr(4)..
				yRange.last.shr(4) + generator.serverLevel.minBuildHeight.shr(4)
			) {
				// Create the deferred now so that they may be awaited on later

				completableSections[chunkSectionY] = CompletableDeferred()
			}

			completableSectionMap[asteroid] = completableSections

			val chunkMinX = chunk.pos.x.shl(4)
			val chunkMinZ = chunk.pos.z.shl(4)

			for ((sectionPos, deferred) in completableSections) {
				generateSection(
					scope,
					asteroid,
					deferred,
					sectionPos,
					chunkMinX,
					chunkMinZ,
					cave1,
					cave2,
					sizeFactor,
					shapingNoise,
					materialNoise,
					taskRandom
				)
			}
		}

		complete(completableSectionMap)
	}

	private suspend fun complete(sections: Map<AsteroidGenerationData, Map<Int, CompletableDeferred<CompletedSection>>>) {
		val completedSections = mutableMapOf<Int, CompletedSection>()

		sections.flatMap { it.value.values }.awaitAll()

		for ((_, sectionsMap) in sections) {
			for ((position, deferredSection) in sectionsMap) {
				val finishedSection = deferredSection.await()

//				val existingSection = completedSections[position]?.let {
//					combineSection(it, finishedSection)
//				} ?: finishedSection // TODO combining block storage

				completedSections[position] = finishedSection
			}
		}

		returnData.complete(StoredChunkBlocks(completedSections.values.toList()))
	}

	/**
	 * Generates one level chunk section (16 * 16 * 16)
	 * @param completable pre-created completable future that is completed by this function
	 **/
	private fun generateSection(
		scope: CoroutineScope,
		asteroid: AsteroidGenerationData,
		completable: CompletableDeferred<CompletedSection>,
		sectionY: Int,
		chunkMinX: Int,
		chunkMinZ: Int,
		cave1: PerlinOctaveGenerator,
		cave2: PerlinOctaveGenerator,
		sizeFactor: Double,
		shapingNoise: SimplexOctaveGenerator,
		materialNoise: SimplexOctaveGenerator,
		taskRandom: Random
	): CompletableDeferred<CompletedSection> {
		scope.launch {
			val palette = mutableListOf<BlockState>()
			val storedBlocks = IntArray(4096)
			val sectionMinY = sectionY.shl(4)

			palette.add(Blocks.AIR.defaultBlockState())
			val paletteListTag = ListTag()

			for (x in 0..15) {
				val worldX = chunkMinX + x
				val worldXDouble = worldX.toDouble()
				val xSquared = (worldXDouble - asteroid.x) * (worldXDouble - asteroid.x)

				for (z in 0..15) {
					val worldZ = chunkMinZ + z
					val worldZDouble = worldZ.toDouble()
					val zSquared = (worldZDouble - asteroid.z) * (worldZDouble - asteroid.z)

					for (y in 0..15) {
						val worldY = sectionMinY + y
						val worldYDouble = worldY.toDouble()
						val ySquared = (worldYDouble - asteroid.y) * (worldYDouble - asteroid.y)

						val index = CompletedSection.posToIndex(x, y, z)

						val isCave: Boolean =
								(abs(cave1.noise(worldXDouble, worldYDouble, worldZDouble, 1.0, 1.0)) < 0.07)
								&&
									(abs(cave2.noise(worldXDouble, worldYDouble, worldZDouble, 1.0, 1.0)) < 0.07)

						if (isCave) continue

						var block: BlockState =
							checkBlockPlacement(
								asteroid,
								worldXDouble,
								worldYDouble,
								worldZDouble,
								xSquared,
								ySquared,
								zSquared,
								sizeFactor,
								shapingNoise,
								materialNoise
							) ?: continue

						if ((
									taskRandom.nextDouble(0.0, 1.0) <= asteroid.oreRatio) && !block.isAir
						) {
							val ore = generator.weightedOres[asteroid.paletteID]!!.random()
							block = generator.oreMap[ore]!!
						}

						val blockIndex =
							if (!palette.contains(block)) {
								palette.add(block)
								palette.lastIndex
							} else palette.indexOf(block)

						storedBlocks[index] = blockIndex
					}
				}
			}

			palette.forEach { blockState -> paletteListTag.add(NbtUtils.writeBlockState(blockState)) }

			completable.complete(
				CompletedSection(
					sectionY,
					palette.map { BlockData(it, null) }.toMutableList(),
					storedBlocks
				)
			)
		}

		return completable
	}

	/**
	 * Places the asteroid block, if inside, and returns the block
	 **/
	private fun checkBlockPlacement(
		asteroid: AsteroidGenerationData,
		worldX: Double,
		worldY: Double,
		worldZ: Double,
		worldXSquared: Double,
		worldYSquared: Double,
		worldZSquared: Double,
		sizeFactor: Double,
		shapingNoise: SimplexOctaveGenerator,
		materialNoise: SimplexOctaveGenerator
	): BlockState? {
		// Calculate a noise pattern with a minimum at zero, and a max peak of the size of the materials list.
		val paletteSample = (
				(
						materialNoise.noise(
							worldX,
							worldY,
							worldZ,
							1.0,
							1.0,
							true
						) + 1
						) / 2
				)

		// Full noise is used as the radius of the asteroid, and it is offset by the noise of each block pos.
		var fullNoise = 0.0
		val initialScale = 0.015 / sizeFactor.coerceAtLeast(1.0)

		for (octave in 0..asteroid.octaves) {
			shapingNoise.setScale(initialScale * (octave + 1.0).pow(2.25 + (sizeFactor / 2.25).coerceAtMost(0.5)))

			val offset = abs(
				shapingNoise.noise(worldX, worldY, worldZ, 0.0, 1.0, false)
			) * (asteroid.size / (octave + 1.0).pow(2.25))

			fullNoise += offset
		}

		fullNoise *= fullNoise
		// Continue if block is not inside any asteroid
		if (worldXSquared + worldYSquared + worldZSquared >= fullNoise) return null

		return asteroid.palette.getEntry(paletteSample)
	}
}

/**
 * This class contains information passed to the generation function.
 * @param [x, y ,z] Origin of the asteroid.
 * @param palette A weighted list of blocks.
 * @param size The radius of the asteroid before noise deformation.
 * @param octaves The number of octaves of noise to apply. Generally 1, but higher for small asteroids. Increases roughness.
 **/
data class AsteroidGenerationData(
	val seed: Long,
	override val x: Int,
	override val y: Int,
	override val z: Int,
	val oreRatio: Double,
	val palette: WeightedRandomList<BlockState>,
	val paletteID: Int,
	val size: Double,
	val octaves: Int,
) : SpaceGenerationData()
