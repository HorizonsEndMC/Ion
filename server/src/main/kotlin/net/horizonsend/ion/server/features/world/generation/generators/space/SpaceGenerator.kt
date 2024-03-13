package net.horizonsend.ion.server.features.world.generation.generators.space

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.space.encounters.Encounters
import net.horizonsend.ion.server.features.world.generation.WorldGenerationManager
import net.horizonsend.ion.server.features.world.generation.generators.interfaces.WorldGenerator
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.distance
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.util.Optional
import java.util.Random
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * This class covers asteroid and wreck generation.
 * An instance of this class is generated for each world on server startup
 *
 **/
class SpaceGenerator(
	val world: World,
	val configuration: ServerConfiguration.AsteroidConfig,
) : WorldGenerator {
	private val spaceGenerationVersion: Byte = 0
	val random = Random(world.seed)

	// ASTEROIDS SECTION
	val oreMap: Map<String, BlockState> = configuration.blockPalettes.flatMap { it.ores }.associate {
		it.material to it.blockState
	}

	// Palettes weighted
	private val weightedPalettes = configuration.paletteWeightedList()

	val weightedOres = configuration.blockPalettes.associate { configuration.blockPalettes.indexOf(it) to oreWeights(it) }

	/** Generates a random asteroid using the world configuration */
	private fun generateRandomAsteroid(
		chunkSeed: Long,
		chunkRandom: Random,
		maxHeight: Int,
		minHeight: Int,
		x: Int,
		y: Int,
		z: Int
	): AsteroidGenerationData {
		var newY = y

		fun generateSize(): Double {
			val newSize = chunkRandom.nextDouble(10.0, configuration.maxAsteroidSize)
			val downShift: Boolean = y + newSize > maxHeight
			val upShift: Boolean = y - newSize < minHeight

			if (upShift) newY = (y - (y + newSize - maxHeight)).toInt()
			if (downShift) newY = (y + (y - newSize + minHeight)).toInt()

			return newSize
		}

		val size = generateSize()
		val palette = weightedPalettes.random(chunkRandom)

		return AsteroidGenerationData(
			chunkSeed,
			x,
			newY,
			z,
			configuration.blockPalettes[palette.first].oreRatio,
			palette.second,
			palette.first,
			size,
			2
		)
	}

	fun parseDensity(x: Double, y: Double, z: Double): Double {
		val densities = mutableSetOf<Double>()
		densities.add(configuration.baseAsteroidDensity)

		for (feature in configuration.features) {
			if (feature.origin.world != world.name) continue

			if ((sqrt((x - feature.origin.x).pow(2) + (z - feature.origin.z).pow(2)) - feature.tubeSize).pow(2) + (y - feature.origin.y).pow(
					2
				) < feature.tubeRadius.pow(2)
			) {
				densities.add(feature.baseDensity)
			}
		}

		return densities.max()
	}

	private fun oreWeights(palette: ServerConfiguration.AsteroidConfig.Palette): WeightedRandomList<String> {
		val weightedList = WeightedRandomList<String>()

		weightedList.addMany(palette.ores.associate { it.material to it.rolls })

		return weightedList
	}
	// Asteroids end

	// Wrecks start
	val schematicCache: LoadingCache<String, Optional<Clipboard>> = CacheBuilder.newBuilder().build(
		CacheLoader.from { schematicName ->
			val file: File = IonServer.dataFolder.resolve("wrecks").resolve("$schematicName.schem")

			return@from Optional.ofNullable(readSchematic(file))
		}
	)

	private fun <T> search(
		chunkPos: ChunkPos,
		initialSeedOffset: Int = 0,
		densityMultiplier: Double,
		callback: (chunkSeed: Long, chunkRandom: Random, maxHeight: Int, minHeight: Int, x: Int, y: Int, z: Int) -> T?
	): List<T> {
		val (centreChunkX, centreChunkZ) = chunkPos

		val radius = 10
		val radiusSquared = radius * radius

		val foundFeatures = mutableListOf<T>()

		val minHeight = world.minHeight
		val maxHeight = world.maxHeight
		val middleHeight = maxHeight / 2.0

		for (chunkXOffset in -radius..+radius) {
			val chunkXOffsetSquared = chunkXOffset * chunkXOffset
			val chunkX = chunkXOffset + centreChunkX
			val startX = chunkX.shl(4)
			val startXDouble = startX.toDouble()

			for (chunkZOffset in -radius..+radius) {
				val chunkZOffsetSquared = chunkZOffset * chunkZOffset
				val chunkZ = chunkZOffset + centreChunkZ
				val startZ = chunkZ.shl(4)
				val startZDouble = startZ.toDouble()

				var seedAdjust = initialSeedOffset

				if (chunkXOffsetSquared + chunkZOffsetSquared > radiusSquared) continue

				val chunkSeed = ChunkPos(chunkX, chunkZ).longKey

				val chunkDensity = parseDensity(startXDouble, middleHeight, startZDouble) * densityMultiplier

				// random number out of 100, chance of asteroid's generation. For use in selection.
				for (count in 0..ceil(chunkDensity).toInt()) {

					// ensure placement of subsequent asteroids and wrecks aren't at the same coordinates
					val chunkRandom = Random(Random(chunkSeed).nextLong() + seedAdjust)
					seedAdjust += 33601

					val chance = chunkRandom.nextDouble(100.0)

					// Selects some asteroids that are generated. Allows for densities of 0<X<1 asteroids per chunk.
					if (chance > (chunkDensity * 10)) continue

					// Random coordinate generation.
					val x = chunkRandom.nextInt(0, 15) + startX
					val z = chunkRandom.nextInt(0, 15) + startZ
					val y = chunkRandom.nextInt(minHeight, maxHeight)

					foundFeatures.add(callback(chunkSeed, chunkRandom, maxHeight, minHeight, x, y, z) ?: continue)
				}
			}
		}

		return foundFeatures
	}

	override suspend fun generateChunk(chunk: Chunk) {
		if (chunk.persistentDataContainer.has(NamespacedKeys.SPACE_GEN_VERSION)) return

		chunk.persistentDataContainer.set(NamespacedKeys.SPACE_GEN_VERSION, PersistentDataType.BYTE, spaceGenerationVersion)

		val chunkPos = chunk.minecraft.pos

		// Generate a number of random asteroids in a chunk, proportional to the density in a portion of the chunk. Allows densities of X>1 asteroid per chunk.

		// Asteroids
		val cornerX = chunkPos.x.shl(4)
		val cornerZ = chunkPos.z.shl(4)

		val acquiredAsteroids = search(
			chunkPos,
			-4207097,
			1.0
		) { chunkSeed, chunkRandom, maxHeight, minHeight, x, y, z ->
			val asteroid = generateRandomAsteroid(chunkSeed, chunkRandom, maxHeight, minHeight, x, y, z)
			val distance = distance(cornerX, cornerZ, asteroid.x, asteroid.z)
			if (distance > (asteroid.size * 1.25)) null else asteroid
		}

		val acquiredWrecks = if (configuration.wreckClasses.isNotEmpty()) {
			search(
				chunkPos,
				4207097,
				configuration.wreckMultiplier
			) { _, chunkRandom, _, _, x, y, z ->
				val wreckClass = configuration.weightedWreckList.random(chunkRandom)
				val wreck = wreckClass.random(chunkRandom)
				val encounter = wreck.encounterWeightedRandomList.randomOrNull(chunkRandom)?.let { Encounters[it] }

				WreckGenerationData(
					x,
					y,
					z,
					wreck.wreckSchematicName,
					encounter
				)
			}
		} else listOf()

		if (acquiredAsteroids.isEmpty() && acquiredWrecks.isEmpty()) return //@runBlocking

		WorldGenerationManager.handleGeneration(
			GenerateChunk(this, chunk.minecraft, acquiredWrecks, acquiredAsteroids),
			WorldGenerationManager.coroutineScope
		)
	}
}

abstract class SpaceGenerationData {
	abstract val x: Int
	abstract val y: Int
	abstract val z: Int
}
