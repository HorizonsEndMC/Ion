package net.horizonsend.ion.server.features.space.generation.generators

import com.sk89q.jnbt.NBTInputStream
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.SpongeSchematicReader
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.space.encounters.Encounter
import net.horizonsend.ion.server.features.space.encounters.Encounters
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.WeightedRandomList
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.nms
import net.starlegacy.util.timing
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.craftbukkit.v1_19_R2.CraftChunk
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.io.ByteArrayInputStream
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
	val configuration: ServerConfiguration.AsteroidConfig
) {
	val spaceGenerationVersion: Byte = 0
	val timing = timing("Space Generation")
	val random = Random(serverLevel.seed)

	abstract class SpaceGenerationData {
		abstract val x: Int
		abstract val y: Int
		abstract val z: Int
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
	) : SpaceGenerationData()

	// ASTEROIDS SECTION
	val oreMap: Map<String, BlockState> = configuration.ores.associate {
		it.material to Bukkit.createBlockData(it.material).nms
	}

	// World asteroid palette noise
	private val worldSimplexNoise = SimplexOctaveGenerator(random, 1).apply { this.setScale(0.0005) }

	// Palettes weighted
	private val weightedPalettes = configuration.paletteWeightedList()

	val weightedOres = oreWeights()

	// Multiple of the radius of the asteroid to mark chunks as might contain an asteroid
	val searchRadius = 1.25

	/**
	 * Generates an asteroid with optional specification for the parameters
	 **/
	fun generateWorldAsteroid(
		x: Int,
		y: Int,
		z: Int,
		size: Double? = null,
		index: Int? = null,
		octaves: Int? = null
	): AsteroidGenerationData {
		val formattedSize = size ?: random.nextDouble(10.0, configuration.maxAsteroidSize)

		val b = weightedPalettes.getEntry(
			(
				worldSimplexNoise.noise(
					x.toDouble(),
					z.toDouble(),
					1.0,
					1.0,
					true
				) + 1
				) / 2
		)

		val blockPalette = index?.let {
			if (!IntRange(0, configuration.blockPalettes.size - 1).contains(index)) {
				throw IndexOutOfBoundsException("ERROR: index out of range: 0..${configuration.blockPalettes.size - 1}")
			}
			weightedPalettes[it]
		} ?: b

		val formattedOctaves = octaves ?: floor(3 * 0.998.pow(formattedSize)).toInt().coerceAtLeast(1)

		return AsteroidGenerationData(x, y, z, blockPalette, formattedSize, formattedOctaves)
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

	private fun oreWeights(): List<ServerConfiguration.AsteroidConfig.Ore> {
		val weightedList = mutableListOf<ServerConfiguration.AsteroidConfig.Ore>()

		for (ore in configuration.ores) {
			for (occurrence in ore.rolls downTo 0) {
				weightedList.add(ore)
			}
		}

		return weightedList
	}
	// Asteroids end

	// Wrecks start
	val schematicMap = configuration.wreckClasses.flatMap { wreckClass -> wreckClass.wrecks }.associate { wreck ->
		wreck.wreckSchematicName to schematic(wreck.wreckSchematicName)
	}

	private fun schematic(schematicName: String): Clipboard {
		val file: File = IonServer.Ion.dataFolder.resolve("wrecks").resolve("$schematicName.schem")

		return SpongeSchematicReader(NBTInputStream(GZIPInputStream(FileInputStream(file)))).read()
	}

	/**
	 * This information is not serialized. It is used in the generation of the wreck.
	 * @param schematicName The name of the schematic file referenced, not including file extension
	 * @param encounter The optional encounter data.
	 **/
	data class WreckGenerationData(
		override val x: Int,
		override val y: Int,
		override val z: Int,
		val schematicName: String,
		val encounter: WreckEncounterData?
	) : SpaceGenerationData() {
		/**
		 * This is serialized and stored in the chunk alongside the wreck.
		 *
		 * @param identifier The identifier string for the encounter class
		 **/
		data class WreckEncounterData(
			val identifier: String,
			val additonalInfo: String?
		) {
			fun getEncounter(): Encounter = Encounters.getByIdentifier(identifier)!!

			fun nms(x: Int, y: Int, z: Int): CompoundTag {
				val beginningTag = CompoundTag()

				beginningTag.putInt("x", x)
				beginningTag.putInt("y", y)
				beginningTag.putInt("z", z)

				beginningTag.putString("Encounter Identifier", identifier)

				return beginningTag
			}
		}
	}

	fun generateRandomWreckData(x: Int, y: Int, z: Int): WreckGenerationData {
		val wreckClass = configuration.weightedWreckList.random()
		val wreck = wreckClass.random()
		val encounter = wreck.encounterWeightedRandomList.random()

		return WreckGenerationData(
			x,
			y,
			z,
			wreck.wreckSchematicName,
			WreckGenerationData.WreckEncounterData(
				encounter,
				null
			)
		)
	}

	companion object {
		fun rebuildChunkAsteroids(chunk: Chunk) {
			val storedAsteroidData =
				chunk.persistentDataContainer.get(NamespacedKeys.STORED_CHUNK_BLOCKS, PersistentDataType.BYTE_ARRAY)
					?: return
			val nbt = try {
				NbtIo.readCompressed(ByteArrayInputStream(storedAsteroidData, 0, storedAsteroidData.size))
			} catch (error: Error) {
				error.printStackTrace(); throw Throwable("Could not serialize stored asteroid data!")
			}

			val levelChunk = (chunk as CraftChunk).handle
			val sections = nbt.getList("sections", 10) // 10 is compound tag, list of compound tags

			val chunkOriginX = chunk.x.shl(4)
			val chunkOriginZ = chunk.z.shl(4)

			for (section in sections) {
				val compound = section as CompoundTag
				val levelChunkSection = levelChunk.sections[compound.getByte("y").toInt()]
				val blocks: IntArray = compound.getIntArray("blocks")
				val paletteList = compound.getList("palette", 10)

				val holderLookup = levelChunk.level.level.holderLookup(Registries.BLOCK)

				var index = 0

				val sectionMinY = levelChunkSection.bottomBlockY()

				for (x in 0..15) {
					val worldX = x + chunkOriginX

					for (z in 0..15) {
						val worldZ = z + chunkOriginZ

						for (y in 0..15) {
							val block = NbtUtils.readBlockState(holderLookup, paletteList[blocks[index]] as CompoundTag)
							if (block == Blocks.AIR.defaultBlockState()) {
								index++
								continue
							}
							levelChunkSection.setBlockState(x, y, z, block)
							levelChunk.playerChunk?.blockChanged(BlockPos(worldX, y + sectionMinY, worldZ))

							index++
						}
					}
				}
			}

			levelChunk.playerChunk?.broadcastChanges(levelChunk)
		}
	}
}
