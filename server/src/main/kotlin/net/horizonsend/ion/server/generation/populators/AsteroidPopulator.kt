package net.horizonsend.ion.server.generation.populators

import java.util.Random
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt
import net.horizonsend.ion.server.generation.configuration.AsteroidConfiguration
import net.horizonsend.ion.server.generation.configuration.AsteroidFeatures
import net.horizonsend.ion.server.generation.configuration.Palette
import net.minecraft.core.BlockPos
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import org.bukkit.util.noise.SimplexOctaveGenerator
import kotlin.math.floor
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer

class AsteroidPopulator : BlockPopulator() {
	// default asteroid configuration values
	private val configuration: AsteroidConfiguration =
		loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_configuration.conf")

	// features (e.g. asteroid belts)
	private val features: AsteroidFeatures =
		loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_features.conf")

	override fun populate(
		worldInfo: WorldInfo,
		random: Random,
		chunkX: Int,
		chunkZ: Int,
		limitedRegion: LimitedRegion,
	) {
		val worldX = chunkX * 16
		val worldZ = chunkZ * 16

		val chunkDensity = parseDensity(worldInfo, worldX.toDouble(), worldInfo.maxHeight / 2.0, worldZ.toDouble())

		// Generate a number of random asteroids in a chunk, proportional to the density in a portion of the chunk. Allows densities of X>1 asteroid per chunk.
		for (count in 0..ceil(chunkDensity).toInt()) {
			val asteroidRandom = Random(System.currentTimeMillis() + worldInfo.seed)

			//Random coordinate generation.
			val asteroidX = asteroidRandom.nextInt(0, 15) + worldX
			val asteroidZ = asteroidRandom.nextInt(0, 15) + worldZ
			val asteroidY = asteroidRandom.nextInt(worldInfo.minHeight + 10, worldInfo.maxHeight - 10)

			// random number out of 100, chance of asteroid's generation. For use in selection.
			val chance = random.nextDouble(100.0)
//			val chance = abs((abs(BlockPos(asteroidX, asteroidY, asteroidZ).hashCode()) % 200.0) - 100.0)

			println("chance: $chance, density: ${chunkDensity * 10}")

			// Selects some asteroids that are generated. Allows for densities of 0<X<1 asteroids per chunk.
			if (chance > (chunkDensity * 10)) continue

			val asteroidLoc = BlockPos(asteroidX, asteroidY, asteroidZ)

			val asteroid = generateAsteroid(asteroidLoc, asteroidRandom)

			if (asteroid.size + asteroidY > worldInfo.maxHeight) continue

			if (asteroidY - asteroid.size < worldInfo.minHeight) continue

			placeAsteroid(worldInfo.seed, chunkX, chunkZ, limitedRegion, asteroid)
		}
	}

	fun placeAsteroid(
		seed: Long,
		chunkX: Int,
		chunkZ: Int,
		limitedRegion: LimitedRegion,
		asteroid: Asteroid,
	) {
		val noise = SimplexOctaveGenerator(Random(seed), 1)

		val worldX = chunkX * 16
		val worldZ = chunkZ * 16

		val blockPos = BlockPos.MutableBlockPos(worldX, 0, worldZ)

		for (x in worldX - limitedRegion.buffer..worldX + 15 + limitedRegion.buffer) {
			val xDouble = x.toDouble()
			val xSquared = (xDouble - asteroid.location.x) * (xDouble - asteroid.location.x)
			blockPos.x = x

			for (z in worldZ - limitedRegion.buffer..worldZ + 15 + limitedRegion.buffer) {
				val zDouble = z.toDouble()
				val zSquared = (zDouble - asteroid.location.z) * (zDouble - asteroid.location.z)
				blockPos.z = z

				for (y in (asteroid.location.y - (2 * asteroid.size)).toInt() until (asteroid.location.y + (2 * asteroid.size)).toInt()) {
					val yDouble = y.toDouble()
					val ySquared = (yDouble - asteroid.location.y) * (yDouble - asteroid.location.y)
					blockPos.y = y

					noise.setScale(0.15)

					var fullNoise =
						0.0 // Full noise is used as the radius of the asteroid, and it is offset by the noise of each block pos.

					for (octave in 0..asteroid.octaves) {
						noise.setScale(0.015 * (octave + 1.0).pow(2.25))

						val offset = abs(
							noise.noise(
								xDouble,
								yDouble,
								zDouble,
								0.0,
								1.0,
								false
							)
						) * (asteroid.size / (octave + 1.0).pow(2.25))

						fullNoise += offset
					}

					if (
						xSquared +
						ySquared +
						zSquared
						> (fullNoise).pow(2)
					) continue // Continue if block is not inside any asteroid

					if (!limitedRegion.isInRegion(x, y, z)) continue

					val weightedMaterials = materialWeights(asteroid.palette)

					noise.setScale(0.15)

					val paletteSample = (((noise.noise(
						worldX + xDouble,
						yDouble,
						worldZ + zDouble,
						1.0,
						1.0,
						true
					) + 1) / 2) * (weightedMaterials.size - 1)).toInt() // Calculate a noise pattern with a minimum at zero, and a max peak of the size of the materials list.

					val material =
						weightedMaterials[paletteSample] // Weight the list by adding duplicate entries, then sample it for the material.

					limitedRegion.setType(x, y, z, material)
				}
			}
		}
	}

	fun postGenerateAsteroid(
		world: World,
		chunkX: Int,
		chunkZ: Int,
		asteroid: Asteroid,
	) {
		val noise = SimplexOctaveGenerator(Random(world.seed), 1)

		val worldX = chunkX * 16
		val worldZ = chunkZ * 16

		val blockPos = BlockPos.MutableBlockPos(worldX, 0, worldZ)

		for (x in worldX - (asteroid.size * 1.5).toInt()..worldX + 15 + (asteroid.size * 1.5).toInt()) {
			val xDouble = x.toDouble()
			val xSquared = (xDouble - asteroid.location.x) * (xDouble - asteroid.location.x)
			blockPos.x = x

			for (z in worldZ - (asteroid.size * 1.5).toInt()..worldZ + 15 + (asteroid.size * 1.5).toInt()) {
				val zDouble = z.toDouble()
				val zSquared = (zDouble - asteroid.location.z) * (zDouble - asteroid.location.z)
				blockPos.z = z

				for (y in (asteroid.location.y - (1.5 * asteroid.size)).toInt() until (asteroid.location.y + (1.5 * asteroid.size)).toInt()) {
					val yDouble = y.toDouble()
					val ySquared = (yDouble - asteroid.location.y) * (yDouble - asteroid.location.y)
					blockPos.y = y

					noise.setScale(0.15)

					var fullNoise =
						0.0 // Full noise is used as the radius of the asteroid, and it is offset by the noise of each block pos.

					for (octave in 0..asteroid.octaves) {
						noise.setScale(0.015 * (octave + 1.0).pow(2.25))

						val offset = abs(
							noise.noise(
								xDouble,
								yDouble,
								zDouble,
								0.0,
								1.0,
								false
							)
						) * (asteroid.size / (octave + 1.0).pow(2.25))

						fullNoise += offset
					}

					if (
						xSquared +
						ySquared +
						zSquared
						> (fullNoise).pow(2)
					) continue // Continue if block is not inside any asteroid

					val weightedMaterials = materialWeights(asteroid.palette)

					noise.setScale(0.15)

					val paletteSample = (((noise.noise(
						worldX + xDouble,
						yDouble,
						worldZ + zDouble,
						1.0,
						1.0,
						true
					) + 1) / 2) * (weightedMaterials.size - 1)).toInt() // Calculate a noise pattern with a minimum at zero, and a max peak of the size of the materials list.

					val material =
						weightedMaterials[paletteSample] // Weight the list by adding duplicate entries, then sample it for the material.

					world.setBlockData(x, y, z, material.createBlockData())
				}
			}
		}

		storeAsteroid(asteroid)
	}

	fun generateAsteroid(location: BlockPos, random: Random): Asteroid {
		val noise = SimplexOctaveGenerator(random, 1)

		// Get material palette

		noise.setScale(0.15)

		val weightedPalette = paletteWeights()
		val paletteSample = random.nextInt(weightedPalette.size)

		val blockPalette: Palette = weightedPalette[paletteSample]

		val size = random.nextDouble(5.0, 20.0)
		val octaves = floor(5 * 0.95.pow(size)).toInt()

		return Asteroid(location, blockPalette, size, octaves)
	}

	private fun parseDensity(worldInfo: WorldInfo, x: Double, y: Double, z: Double): Double {
		val densities = mutableSetOf<Double>()
		densities.add(configuration.baseAsteroidDensity)

		for (feature in features.features) {
			if (feature.worldName != worldInfo.name) continue

			if ((sqrt((x - feature.x).pow(2) + (z - feature.z).pow(2)) - feature.tubeSize).pow(2) + (y - feature.y).pow(
					2) < feature.tubeRadius.pow(2)
			) {
				densities.add(feature.baseDensity)
			}
		}

		return densities.max()
	}

	/**
	 * Weights the list of Palettes in the configuration by adding duplicate entries based on the weight.
	 */
	private fun paletteWeights(): List<Palette> {
		val weightedList = mutableListOf<Palette>()

		for (palette in configuration.blockPalettes) {
			for (occurrence in palette.weight downTo 0) {
				weightedList.add(palette)
			}
		}

		return weightedList
	}

	private fun materialWeights(palette: Palette): List<Material> {
		val weightedList = mutableListOf<Material>()

		for (material in palette.materials) {
			for (occurrence in material.value downTo 0) {
				weightedList.add(material.key)
			}
		}

		return weightedList
	}

	fun storeAsteroid(asteroid: Asteroid) {

	}

	data class Asteroid(
		val location: BlockPos,
		val palette: Palette,
		val size: Double,
		val octaves: Int
	)
}