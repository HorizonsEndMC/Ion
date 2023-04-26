package net.horizonsend.ion.server.features.space.generation.generators

import com.sk89q.jnbt.NBTInputStream
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.SpongeSchematicReader
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.place
import net.horizonsend.ion.server.features.space.encounters.Encounters
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.WeightedRandomList
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Chunk
import java.io.File
import java.io.FileInputStream
import java.util.Random
import java.util.zip.GZIPInputStream
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * This class covers asteroid and wreck generation.
 * An instance of this class is generated for each world on server startup
 *
 **/
class SpaceGenerator(
	val serverLevel: ServerLevel,
	val configuration: ServerConfiguration.AsteroidConfig,
) {
	val spaceGenerationVersion: Byte = 0
	val random = Random(serverLevel.seed)

	// ASTEROIDS SECTION
	val oreMap: Map<String, BlockState> = configuration.blockPalettes.flatMap { it.ores }.associate {
		it.material to it.blockState
	}

	// Palettes weighted
	private val weightedPalettes = configuration.paletteWeightedList()

	val weightedOres = configuration.blockPalettes.associate { configuration.blockPalettes.indexOf(it) to oreWeights(it) }

	// Multiple of the radius of the asteroid to mark chunks as might contain an asteroid
	val searchRadius = 1.0

	/**
	 * Generates an asteroid with optional specification for the parameters
	 **/
	fun generateWorldAsteroid(
		chunkSeed: Long,
		chunkRandom: Random,
		maxHeight: Int,
		minHeight: Int,
		x: Int,
		y: Int,
		z: Int,
		size: Double? = null,
		index: Int? = null,
		octaves: Int? = null,
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

		val formattedSize = size ?: generateSize()

		val palette = weightedPalettes.random(chunkRandom)

		val oreRatio = index?.let {
			configuration.blockPalettes[it].oreRatio
		} ?: configuration.blockPalettes[palette.first].oreRatio

		val blockPalette = index?.let {
			if (!IntRange(0, configuration.blockPalettes.size - 1).contains(index)) {
				throw IndexOutOfBoundsException("ERROR: index out of range: 0..${configuration.blockPalettes.size - 1}")
			}
			weightedPalettes[it]
		} ?: palette

		val formattedOctaves = octaves ?: floor(3 * 0.998.pow(formattedSize)).toInt().coerceAtLeast(1)

		return AsteroidGenerationData(
			chunkSeed,
			x,
			newY,
			z,
			oreRatio,
			blockPalette.second,
			blockPalette.first,
			formattedSize,
			formattedOctaves
		)
	}

	fun generateRandomAsteroid(
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
			if (feature.origin.world != serverLevel.serverLevelData.levelName) continue

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
	val schematicMap = configuration.wreckClasses.flatMap { wreckClass -> wreckClass.wrecks }.associate { wreck ->
		wreck.wreckSchematicName to schematic(wreck.wreckSchematicName)
	}

	private fun schematic(schematicName: String): Clipboard {
		val file: File = IonServer.dataFolder.resolve("wrecks").resolve("$schematicName.schem")

		return SpongeSchematicReader(NBTInputStream(GZIPInputStream(FileInputStream(file)))).read()
	}

	fun generateRandomWreckData(chunkRandom: Random, x: Int, y: Int, z: Int): WreckGenerationData {
		val wreckClass = configuration.weightedWreckList.random(chunkRandom)
		val wreck = wreckClass.random(chunkRandom)
		val encounter = wreck.encounterWeightedRandomList.randomOrNull(chunkRandom)?.let { Encounters[it] }

		return WreckGenerationData(
			x,
			y,
			z,
			wreck.wreckSchematicName,
			encounter
		)
	}

	companion object {
		fun regenerateChunk(chunk: Chunk) {
			chunk.persistentDataContainer
				.get(
					NamespacedKeys.STORED_CHUNK_BLOCKS,
					StoredChunkBlocks
				)?.place(chunk)
		}
	}
}

abstract class SpaceGenerationData {
	abstract val x: Int
	abstract val y: Int
	abstract val z: Int
}

//fun getHeightMapIndex(x: Int, z: Int) = x + z * 16
//
//val newHeightmap = SimpleBitStorage(Mth.ceillog2(levelChunk.height + 1), 256)
//for ((type, heightMap) in levelChunk.heightmaps) {
//	heightMap.setRawData(levelChunk, type, newHeightmap.raw)
//}
//if (newHeightmap.get(index) < worldY) newHeightmap.set(heightMapIndex, worldY)
