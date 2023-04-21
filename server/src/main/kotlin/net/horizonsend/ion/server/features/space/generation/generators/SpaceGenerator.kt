package net.horizonsend.ion.server.features.space.generation.generators

import com.sk89q.jnbt.NBTInputStream
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.SpongeSchematicReader
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.space.generation.generators.WreckGenerationData.WreckEncounterData
import net.horizonsend.ion.server.features.space.generation.BlockSerialization
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.WeightedRandomList
import net.horizonsend.ion.server.miscellaneous.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.Heightmap
import org.bukkit.Chunk
import org.bukkit.craftbukkit.v1_19_R2.CraftChunk
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
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
	val random = Random(serverLevel.seed)

	// ASTEROIDS SECTION
	val oreMap: Map<String, BlockState> = configuration.blockPalettes.flatMap { it.ores }.associate {
		it.material to it.blockState
	}

	// World asteroid palette noise
	private val worldSimplexNoise = SimplexOctaveGenerator(random, 1).apply { this.setScale(0.0010) }

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
		octaves: Int? = null
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

		val palette = weightedPalettes.getEntry(
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

	fun generateRandomWreckData(x: Int, y: Int, z: Int): WreckGenerationData {
		val wreckClass = configuration.weightedWreckList.random()
		val wreck = wreckClass.random()
		val encounter = wreck.encounterWeightedRandomList.random()

		return WreckGenerationData(
			x,
			y,
			z,
			wreck.wreckSchematicName,
			WreckEncounterData(
				encounter,
				null
			)
		)
	}

	companion object {
		fun regenerateChunk(chunk: Chunk) {
			chunk.persistentDataContainer.get(
				NamespacedKeys.WRECK_ENCOUNTER_DATA,
				PersistentDataType.BYTE_ARRAY
			)?.let { data ->
				val wreckData = try { // get existing asteroid data
					val bos = ByteArrayInputStream(
						data,
						0,
						data.size
					)

					NbtIo.read(
						DataInputStream(bos)
					)
				} catch (error: Error) {
					error.printStackTrace(); return
				}

				val existingChests = wreckData.getList("SecondaryChests", 10)
				val existingWrecks = wreckData.getList("Wrecks", 10)
				wreckData.remove("SecondaryChests")
				wreckData.remove("Wrecks")

				for (existingChest in existingChests) {
					(existingChest as CompoundTag)

					existingChests.remove(existingChest)
					existingChest.putBoolean("inactive", false)
					existingChests.add(existingChest)
				}

				for (existingWreck in existingWrecks) {
					(existingWreck as CompoundTag)

					existingWrecks.remove(existingWreck)
					existingWreck.putBoolean("inactive", false)
					existingWrecks.add(existingWreck)
				}

				wreckData.put("SecondaryChests", existingChests)
				wreckData.put("Wrecks", existingWrecks)

				val byteArray = ByteArrayOutputStream()

				val dataOutput = DataOutputStream(byteArray)
				NbtIo.write(wreckData, dataOutput)

				// Update PDCs
				chunk.persistentDataContainer.set(
					NamespacedKeys.WRECK_ENCOUNTER_DATA,
					PersistentDataType.BYTE_ARRAY,
					byteArray.toByteArray()
				)
			}

			buildChunkBlocks(chunk)
		}

		private fun buildChunkBlocks(chunk: Chunk) {
			val levelChunk = (chunk as CraftChunk).handle

			val storedAsteroidData =
				chunk.persistentDataContainer.get(NamespacedKeys.STORED_CHUNK_BLOCKS, PersistentDataType.BYTE_ARRAY)
					?: return
			val nbt = try {
				val bos = ByteArrayInputStream(
					storedAsteroidData,
					0,
					storedAsteroidData.size
				)

				NbtIo.read(
					DataInputStream(bos)
				)
			} catch (error: Error) {
				error.printStackTrace(); throw Throwable("Could not serialize stored asteroid data!")
			}

			val sections = nbt.getList("sections", 10) // 10 is compound tag, list of compound tags

			val chunkOriginX = chunk.x.shl(4)
			val chunkOriginZ = chunk.z.shl(4)

			for (section in sections) {
				val compound = (section as CompoundTag).getCompound("block_states")
				val levelChunkSection = levelChunk.sections[section.getByte("y").toInt()]

				val blocks: IntArray = compound.getIntArray("data")
				val paletteList = compound.getList("palette", 10)

				val holderLookup = levelChunk.level.level.holderLookup(Registries.BLOCK)

				val sectionMinY = levelChunkSection.bottomBlockY()

				for (x in 0..15) {
					val worldX = x + chunkOriginX

					for (z in 0..15) {
						val worldZ = z + chunkOriginZ

						for (y in 0..15) {
							val worldY = y + sectionMinY

							val entry = paletteList[blocks[BlockSerialization.posToIndex(x, y, z)]]

							val block = NbtUtils.readBlockState(holderLookup, entry as CompoundTag)
							if (block == Blocks.AIR.defaultBlockState()) {
								continue
							}

							val tileEntity: CompoundTag = entry.getCompound("TileEntity")

							levelChunkSection.setBlockState(x, y, z, block)

							if (!tileEntity.isEmpty) {
								let {
									val blockEntity = BlockEntity.loadStatic(
										BlockPos(worldX, worldY, worldZ),
										block,
										tileEntity
									) ?: return@let

									levelChunk.addAndRegisterBlockEntity(blockEntity)
								}
							}

							levelChunk.playerChunk?.blockChanged(BlockPos(worldX, worldY, worldZ))
						}
					}
				}
			}

			levelChunk.playerChunk?.broadcastChanges(levelChunk)

			Heightmap.primeHeightmaps(levelChunk, Heightmap.Types.values().toSet())
			levelChunk.isUnsaved = true
		}
	}
}

abstract class SpaceGenerationTask<V : SpaceGenerationReturnData> {
	abstract val generator: SpaceGenerator
	abstract val returnData: Deferred<V>
	abstract val chunk: ChunkPos

	abstract suspend fun generateChunk(scope: CoroutineScope)

	// Work to be done after the task has completed, but before it is placed. EG caves
	open fun postProcessSync(completedData: SpaceGenerationReturnData) {}

	// Work to be done sync after the task has completed, but before it is placed. EG placing unsaved blocks
	open fun postProcessASync(completedData: SpaceGenerationReturnData) {}
}

abstract class SpaceGenerationData {
	abstract val x: Int
	abstract val y: Int
	abstract val z: Int
}

abstract class SpaceGenerationReturnData {
	abstract val completedSectionMap: List<CompletedSection>
	data class CompletedSection(
		val y: Int,
		val blocks: IntArray,
		val palette: List<Pair<BlockState, CompoundTag?>>,
		val nmsPalette: ListTag
	)

	open fun finishPlacement(chunk: ChunkPos, generator: SpaceGenerator): Deferred<LevelChunk> {
		val asyncChunk = generator.serverLevel.world.getChunkAtAsync(chunk.x, chunk.z)

		val chunks = CompletableDeferred<LevelChunk>()

		// Using a completable future here, so I can thenAccept while keeping it sync.
		// Using a .await() would require it to be in a suspend function or coroutine.
		asyncChunk.thenAccept {
			for (completedSection in completedSectionMap) {
				val chunkMinX = chunk.x.shl(4)
				val chunkMinZ = chunk.z.shl(4)

				val levelChunk = it.minecraft

				val section = levelChunk.sections[completedSection.y]
				val palette = completedSection.palette

					val sectionMinY = section.bottomBlockY()

					for (x in 0..15) {
						val worldX = x + chunkMinX

						for (z in 0..15) {
							val worldZ = z + chunkMinZ

							for (y in 0..15) {
								val worldY = sectionMinY + y

								val index = BlockSerialization.posToIndex(x, y, z)

								val block = palette[completedSection.blocks[index]]

								if (block.first.isAir) continue

								section.setBlockState(x, y, z, block.first)

								block.second?.let { compoundTag ->
									val blockEntity = BlockEntity.loadStatic(
										BlockPos(worldX, worldY, worldZ),
										block.first,
										compoundTag
									) ?: return@let

									levelChunk.addAndRegisterBlockEntity(blockEntity)
								}

								levelChunk.playerChunk?.blockChanged(
									BlockPos(
										chunkMinX + x,
										sectionMinY + y,
										chunkMinZ + z
									)
								)
							}
						}
					}


				Heightmap.primeHeightmaps(levelChunk, Heightmap.Types.values().toSet())
				levelChunk.playerChunk?.broadcastChanges(levelChunk)
				levelChunk.isUnsaved = true

				chunks.complete(levelChunk)
			}
		}

		return chunks
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun store(generator: SpaceGenerator, chunk: Deferred<LevelChunk>) = chunk.invokeOnCompletion {
		// Cannot use await, since it can only be called from a coroutine. Storage needs to be sync.
		val bukkitChunk = chunk.getCompleted().bukkitChunk

			val existingData = BlockSerialization.readChunkCompoundTag(bukkitChunk, NamespacedKeys.STORED_CHUNK_BLOCKS)
			val existingSectionsList = existingData?.getList("sections", 10)?.toList()
				?.associateBy { (it as CompoundTag).getInt("y") }

			val newChunkData = completedSectionMap

			val newCoveredSections = newChunkData.map { it.y }

			val combinedSections = ListTag()

			val unchanged = existingSectionsList?.filter { !newCoveredSections.contains(it.key) }
			unchanged?.let { combinedSections.addAll(it.values) }

			for ((y, newBlocks, _, nmsPalette) in newChunkData) {
				val existingSection = existingSectionsList?.get(y) as? CompoundTag

				val newSection = BlockSerialization.formatSection(y, newBlocks, nmsPalette)

				val combined = existingSection?.let {
					BlockSerialization.combineSerializedSections(y, existingSection, newSection)
				} ?: newSection

				combinedSections.add(combined)
			}

			val formattedChunk = BlockSerialization.formatChunk(combinedSections, generator.spaceGenerationVersion)

			BlockSerialization.setChunkCompoundTag(bukkitChunk, NamespacedKeys.STORED_CHUNK_BLOCKS, formattedChunk)
	}
}

