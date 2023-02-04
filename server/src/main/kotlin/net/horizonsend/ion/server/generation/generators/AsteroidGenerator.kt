package net.horizonsend.ion.server.generation.generators

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.ServerConfiguration
import net.horizonsend.ion.server.generation.Asteroid
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.LevelChunkSection
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.Random
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

object AsteroidGenerator {
	fun postGenerateAsteroid(
		serverLevel: ServerLevel,
		asteroid: Asteroid
	) {
		val noise = SimplexOctaveGenerator(Random(serverLevel.seed), 1)
		val radiusSquared = asteroid.size * asteroid.size

		// Get every chunk section covered by the asteroid.

		val coveredChunks = mutableMapOf<LevelChunk, List<LevelChunkSection>>()

		val xRange = IntRange(asteroid.x - (asteroid.size * 1.5).toInt(), asteroid.x + (asteroid.size * 1.5).toInt())
		val zRange = IntRange(asteroid.z - (asteroid.size * 1.5).toInt(), asteroid.z + (asteroid.size * 1.5).toInt())
		val yRange = IntRange(
			(asteroid.y - (asteroid.size * 1.5).toInt()).coerceAtLeast(serverLevel.minBuildHeight),
			(asteroid.y + (asteroid.size * 1.5).toInt()).coerceAtMost(serverLevel.maxBuildHeight)
		)

		val chunkXRange = IntRange(xRange.first.shr(4), xRange.last.shr(4))
		val chunkZRange = IntRange(zRange.first.shr(4), zRange.last.shr(4))
		val chunkYRange = IntRange(yRange.first.shr(4), yRange.last.shr(4) + serverLevel.minBuildHeight.shr(4) - 1)

		for (chunkPosX in chunkXRange) {
			val xSqr = (chunkPosX - asteroid.x.shr(4)) * (chunkPosX - asteroid.x.shr(4))

			for (chunkPosZ in chunkZRange) {
				val zSqr = (chunkPosZ - asteroid.z.shr(4)) * (chunkPosZ - asteroid.z.shr(4))
				val circle = xSqr + zSqr

				if (circle >= radiusSquared) continue // if out of equatorial radius continue

				val coveredChunk = serverLevel.getChunk(chunkPosX, chunkPosZ)
				val sections = mutableListOf<LevelChunkSection>()

				for (chunkSectionY in chunkYRange) {
					val ySqr = (chunkSectionY - asteroid.y.shr(4)) * (chunkSectionY - asteroid.y.shr(4))

					if ((circle + ySqr) <= radiusSquared) {
						sections += coveredChunk.getSection(chunkSectionY)
					}
				}

				coveredChunks[coveredChunk] = sections
			}
		}

		// Covered chunks acquired

		// For each covered chunk
		for (chunk in coveredChunks) {
			val nmsChunk = chunk.key

			val chunkX = asteroid.x.shr(4)
			val chunkZ = asteroid.z.shr(4)

			val placedBlocks = arrayOfNulls<BlockState?>(4096)
			// 	val palette = mutableSetOf<BlockState>()

			for (section in chunk.value) {
				for (x in 0..15) {
					val xDouble = x.toDouble()
					val xSquared = ((x + chunkX) - asteroid.x) * ((x + chunkX) - asteroid.x)

					for (z in 0..15) {
						val zDouble = z.toDouble()
						val zSquared = ((z + chunkZ) - asteroid.z) * ((z + chunkZ) - asteroid.z)

						for (y in 0..15) {
							val ySquared = (y - asteroid.y) * (y - asteroid.y)

							val block: BlockState? = placeBlock(
								section,
								nmsChunk.pos.x,
								nmsChunk.pos.z,
								xDouble,
								y.toDouble(),
								zDouble,
								xSquared,
								ySquared,
								zSquared,
								asteroid,
								noise
							)

							val posIndex = x + y + z

							if (block != null) {
								// 	palette.add(block)
								placedBlocks[posIndex] = block
								nmsChunk.playerChunk?.blockChanged(BlockPos(x, y, z))
							} else {
								// 	placedBlocks[posIndex] = Blocks.AIR.defaultBlockState()
							}
						}
					}
				}
			}

			// 	val storedData = AsteroidBlockStorage.generateChunkData(placedBlocks, 0)

			// 	nmsChunk.bukkitChunk.persistentDataContainer

			nmsChunk.playerChunk?.broadcastChanges(nmsChunk)
// 			nmsChunk.bukkitChunk.persistentDataContainer.set(NamespacedKeys.ASTEROIDS, PersistentDataType.TAG_CONTAINER_ARRAY)
		}

		val random: RandomSource = RandomSource.create(serverLevel.seed)

// 		for (
// 		count in ceil(Ion.configuration.oreRatio * asteroid.size.pow(2.75)).toInt() downTo 0
// 		) {
// 			val originX = random.nextInt(
// 				asteroid.x - (asteroid.size * 1.5).toInt(),
// 				asteroid.x + 16 + (asteroid.size * 1.5).toInt()
// 			)
// 			val originY = random.nextInt(
// 				asteroid.y - (asteroid.size * 1.5).toInt(),
// 				asteroid.y + 16 + (asteroid.size * 1.5).toInt()
// 			)
// 			val originZ = random.nextInt(
// 				asteroid.z - (asteroid.size * 1.5).toInt(),
// 				asteroid.z + 16 + (asteroid.size * 1.5).toInt()
// 			)
//
// 			if (!OreGenerator.asteroidBlocks.contains(serverLevel.getBlockState(BlockPos(originX, originY, originZ)))) {
// 				continue
// 			} // Quickly move on if it's not in an asteroid
//
// 			val ore = OreGenerator.weightedOres[random.nextInt(0, OreGenerator.weightedOres.size - 1)]
//
// 			val blobSize = random.nextInt(ore.maxBlobSize).coerceAtLeast(1)
//
// 			OreGenerator.generateOre(world, PlacedOre(ore.material, blobSize, originX, originY, originZ))
// 		}
	}

	/**
	 * Places the asteroid block, if inside, and returns the block
	 * */
	fun placeBlock(
		section: LevelChunkSection,
		sectionX: Int,
		sectionZ: Int,
		x: Double,
		y: Double,
		z: Double,
		xSquared: Int,
		ySquared: Int,
		zSquared: Int,
		asteroid: Asteroid,
		noise: SimplexOctaveGenerator
	): BlockState? {
		noise.setScale(0.15)

		val weightedMaterials = asteroid.materialWeights()

		// Full noise is used as the radius of the asteroid, and it is offset by the noise of each block pos.
		var fullNoise = 0.0

		for (octave in 0..asteroid.octaves) {
			noise.setScale(0.015 * (octave + 1.0).pow(2.25))

			val offset = abs(
				noise.noise(x, y, z, 0.0, 1.0, false)
			) * (asteroid.size / (octave + 1.0).pow(2.25))

			fullNoise += offset
		}

		// Continue if block is not inside any asteroid
		if (xSquared + ySquared + zSquared > (fullNoise).pow(2)) return null

		noise.setScale(0.15)

		// Calculate a noise pattern with a minimum at zero, and a max peak of the size of the materials list.
		val paletteSample = (
			(
				(
					noise.noise(
						asteroid.x + x,
						y,
						asteroid.x + z,
						1.0,
						1.0,
						true
					) + 1
					) / 2
				) * (weightedMaterials.size - 1)
			).toInt()

		// Weight the list by adding duplicate entries, then sample it for the material.
		val material = weightedMaterials[paletteSample]

		println("absolte " + (x + sectionX) + " y " + (y + section.bottomBlockY()) + " z " + (z + sectionZ))

		section.setBlockState(
			x.toInt(),
			y.toInt(),
			z.toInt(),
			material
		)

		return material
	}

	fun generateRandomAsteroid(x: Int, y: Int, z: Int, random: Random): Asteroid {
		val noise = SimplexOctaveGenerator(random, 1)

		noise.setScale(0.15)

		val weightedPalette = paletteWeights()
		val paletteSample = random.nextInt(weightedPalette.size)

		val blockPalette: ServerConfiguration.Palette = weightedPalette[paletteSample]

		val size = random.nextDouble(5.0, Ion.configuration.maxAsteroidSize)
		val octaves = floor(5 * 0.95.pow(size)).toInt().coerceAtLeast(1)

		return Asteroid(x, y, z, blockPalette, size, octaves)
	}

// 	private fun materialWeights(palette: ServerConfiguration.Palette): List<BlockState> {
// 		val weightedList = mutableListOf<BlockState>()
//
// 		for (material in palette.materials) {
// 			for (occurrence in material.value downTo 0) {
// 				weightedList.add(material.key)
// 			}
// 		}
//
// 		return weightedList
// 	}

	fun parseDensity(world: ServerLevel, x: Double, y: Double, z: Double): Double {
		val densities = mutableSetOf<Double>()
		densities.add(Ion.configuration.baseAsteroidDensity)

		for (feature in Ion.configuration.features) {
			if (feature.worldName != world.serverLevelData.levelName) continue

			if ((sqrt((x - feature.x).pow(2) + (z - feature.z).pow(2)) - feature.tubeSize).pow(2) + (y - feature.y).pow(
					2
				) < feature.tubeRadius.pow(2)
			) {
				densities.add(feature.baseDensity)
			}
		}

		return densities.max()
	}

	/**
	 * Weights the list of Palettes in the configuration by adding duplicate entries based on the weight.
	 */
	private fun paletteWeights(): List<ServerConfiguration.Palette> {
		val weightedList = mutableListOf<ServerConfiguration.Palette>()

		for (palette in Ion.configuration.blockPalettes) {
			for (occurrence in palette.weight downTo 0) {
				weightedList.add(palette)
			}
		}

		return weightedList
	}
}
