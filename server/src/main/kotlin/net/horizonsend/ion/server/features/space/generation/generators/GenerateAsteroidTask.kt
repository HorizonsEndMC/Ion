package net.horizonsend.ion.server.features.space.generation.generators

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.miscellaneous.WeightedRandomList
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.util.noise.SimplexOctaveGenerator
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class GenerateAsteroidTask(
	override val generator: SpaceGenerator,
	private val asteroid: AsteroidGenerationData
) : SpaceGenerationTask<AsteroidGenerationData.AsteroidReturnData>() {
	val config = generator.configuration
	private val sizeFactor = asteroid.size / 15
	private val shapingNoise = SimplexOctaveGenerator(generator.random, 1)
	private val materialNoise = SimplexOctaveGenerator(generator.random, 1)

	override val returnData = CompletableDeferred<AsteroidGenerationData.AsteroidReturnData>()

	override fun generate() {
		val sectionMap = mutableMapOf<ChunkPos, List<SpaceGenerationReturnData.CompletedSection>>()

		SpaceGenerationManager.coroutineScope.launch {
			// save some time
			materialNoise.setScale(0.15 / sqrt(sizeFactor))

			val radiusSquared = asteroid.size * asteroid.size

			// Get every chunk section covered by the asteroid.
			val coveredChunks = mutableMapOf<ChunkPos, List<Int>>()

			// generate ranges ahead of time
			val xRange =
				IntRange(asteroid.x - (asteroid.size * generator.searchRadius).toInt(), asteroid.x + (asteroid.size * generator.searchRadius).toInt())
			val zRange =
				IntRange(asteroid.z - (asteroid.size * generator.searchRadius).toInt(), asteroid.z + (asteroid.size * generator.searchRadius).toInt())
			val yRange = IntRange(
				(asteroid.y - (asteroid.size * generator.searchRadius).toInt()).coerceAtLeast(generator.serverLevel.minBuildHeight),
				(asteroid.y + (asteroid.size * generator.searchRadius).toInt()).coerceAtMost(generator.serverLevel.maxBuildHeight)
			)

			val chunkXRange = IntRange(xRange.first.shr(4), xRange.last.shr(4))
			val chunkZRange = IntRange(zRange.first.shr(4), zRange.last.shr(4))
			val chunkYRange = IntRange(yRange.first.shr(4), yRange.last.shr(4) + generator.serverLevel.minBuildHeight.shr(4))

			for (chunkPosX in chunkXRange) {
				val xSqr = (chunkPosX - asteroid.x.shr(4)) * (chunkPosX - asteroid.x.shr(4))

				for (chunkPosZ in chunkZRange) {
					val zSqr = (chunkPosZ - asteroid.z.shr(4)) * (chunkPosZ - asteroid.z.shr(4))
					val circle = xSqr + zSqr

					if (circle >= radiusSquared) continue // if out of equatorial radius continue

					val sections = mutableListOf<Int>()

					for (chunkSectionY in chunkYRange) {
						val ySqr = (chunkSectionY - asteroid.y.shr(4)) * (chunkSectionY - asteroid.y.shr(4))

						if ((circle + ySqr) <= radiusSquared) {
							sections += chunkSectionY
						}
					}

					coveredChunks[ChunkPos(chunkPosX, chunkPosZ)] = sections
				}
			}
			// Covered chunks acquired

			// For each covered chunk
			for ((nmsChunkPos, sectionList) in coveredChunks) {
				val chunkCompletedSections = mutableListOf<SpaceGenerationReturnData.CompletedSection>()

				val chunkMinX = nmsChunkPos.x.shl(4)
				val chunkMinZ = nmsChunkPos.z.shl(4)

				for (sectionPos in sectionList) {
					val newlyCompleted = generateSection(
						sectionPos,
						chunkMinX,
						chunkMinZ
					) ?: continue

					chunkCompletedSections.add(newlyCompleted)
				}

				// Return if chunk has no new blocks
				if (chunkCompletedSections.isEmpty()) continue

				sectionMap[nmsChunkPos] = chunkCompletedSections
			}

			returnData.complete(
				AsteroidGenerationData.AsteroidReturnData(
					sectionMap
				)
			)
		}
	}

	/**
	 * Generates one level chunk section (16 * 16 * 16)
	 *
	 *
	 **/
	private fun generateSection(
		sectionY: Int,
		chunkMinX: Int,
		chunkMinZ: Int
	): SpaceGenerationReturnData.CompletedSection? {
		val palette = mutableSetOf<BlockState>()
		val storedBlocks = arrayOfNulls<Int>(4096)
		var index = 0
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

					var block: BlockState? =
						checkBlockPlacement(
							worldXDouble,
							worldYDouble,
							worldZDouble,
							xSquared,
							ySquared,
							zSquared
						)

					if (
						(
							generator.random.nextDouble(0.0, 1.0) <= generator.configuration.oreRatio &&
								block != null
							) && !block.isAir
					) {
						val ore = generator.weightedOres[generator.random.nextInt(0, generator.weightedOres.size - 1)]
						block = generator.oreMap[ore.material]
					}

					if (block != null) {
						palette.add(block)
						storedBlocks[index] = palette.indexOf(block)
					} else { storedBlocks[index] = 0 }

					index++
				}
			}
		}

		if (storedBlocks.all { it == 0 }) return null // don't write it if it's all empty

		palette.forEach { blockState -> paletteListTag.add(NbtUtils.writeBlockState(blockState)) }
		val intArray = storedBlocks.requireNoNulls().toIntArray()

		return SpaceGenerationReturnData.CompletedSection(
			sectionY,
			intArray,
			palette,
			paletteListTag
		)
	}

	/**
	 * Places the asteroid block, if inside, and returns the block
	 **/
	private fun checkBlockPlacement(
		worldX: Double,
		worldY: Double,
		worldZ: Double,
		worldXSquared: Double,
		worldYSquared: Double,
		worldZSquared: Double
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
	override val x: Int,
	override val y: Int,
	override val z: Int,
	val palette: WeightedRandomList<BlockState>,
	val size: Double,
	val octaves: Int
) : SpaceGenerationData() {

	data class AsteroidReturnData(
		override val completedSectionMap: Map<ChunkPos, List<CompletedSection>>
	) : SpaceGenerationReturnData()
}
