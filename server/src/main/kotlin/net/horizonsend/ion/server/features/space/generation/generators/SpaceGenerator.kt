package net.horizonsend.ion.server.features.space.generation.generators

import com.sk89q.jnbt.NBTInputStream
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.SpongeSchematicReader
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.space.encounters.Encounter
import net.horizonsend.ion.server.features.space.encounters.Encounters
import net.horizonsend.ion.server.features.space.generation.BlockSerialization
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.WeightedRandomList
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
import net.minecraft.world.level.levelgen.Heightmap
import net.starlegacy.util.timing
import org.bukkit.Chunk
import org.bukkit.craftbukkit.v1_19_R2.CraftChunk
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.Random
import java.util.concurrent.CompletableFuture
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

	// ASTEROIDS SECTION
	val oreMap: Map<String, BlockState> = configuration.blockPalettes.flatMap { it.ores }.associate {
		it.material to it.blockState
	}

	// World asteroid palette noise
	private val worldSimplexNoise = SimplexOctaveGenerator(random, 1).apply { this.setScale(0.0005) }

	// Palettes weighted
	private val weightedPalettes = configuration.paletteWeightedList()

	val weightedOres = configuration.blockPalettes.associate { configuration.blockPalettes.indexOf(it) to oreWeights(it) }

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

		return AsteroidGenerationData(x, y, z, blockPalette.second, blockPalette.first, formattedSize, formattedOctaves)
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
		fun regenerateChunk(chunk: Chunk) {
			chunk.persistentDataContainer.get(
				NamespacedKeys.WRECK_ENCOUNTER_DATA,
				PersistentDataType.BYTE_ARRAY
			)?.let { data ->
				val wreckData = try { // get existing asteroid data
					NbtIo.readCompressed(
						ByteArrayInputStream(
							data,
							0,
							data.size
						)
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
				wreckData.put("SecondaryChests", existingWrecks)

				val wreckDataOutputStream = ByteArrayOutputStream()
				NbtIo.writeCompressed(wreckData, wreckDataOutputStream)

				chunk.persistentDataContainer.set(
					NamespacedKeys.WRECK_ENCOUNTER_DATA,
					PersistentDataType.BYTE_ARRAY,
					wreckDataOutputStream.toByteArray()
				)


			}

			buildChunkBlocks(chunk)
		}

		fun buildChunkBlocks(chunk: Chunk) {
			val levelChunk = (chunk as CraftChunk).handle

			val storedAsteroidData =
				chunk.persistentDataContainer.get(NamespacedKeys.STORED_CHUNK_BLOCKS, PersistentDataType.BYTE_ARRAY)
					?: return
			val nbt = try {
				NbtIo.readCompressed(ByteArrayInputStream(storedAsteroidData, 0, storedAsteroidData.size))
			} catch (error: Error) {
				error.printStackTrace(); throw Throwable("Could not serialize stored asteroid data!")
			}

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
							val block =
								NbtUtils.readBlockState(holderLookup, paletteList[blocks[index]] as CompoundTag)
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

			Heightmap.primeHeightmaps(levelChunk, Heightmap.Types.values().toSet())
			levelChunk.isUnsaved = true
		}
	}
}

abstract class SpaceGenerationTask<V : SpaceGenerationReturnData> {
	abstract val generator: SpaceGenerator
	abstract val returnData: Deferred<V>

	abstract fun generate()

	open fun postProcess(completedData: SpaceGenerationReturnData) {}
}

abstract class SpaceGenerationData {
	abstract val x: Int
	abstract val y: Int
	abstract val z: Int
}

abstract class SpaceGenerationReturnData {
	abstract val completedSectionMap: Map<ChunkPos, List<CompletedSection>>
	data class CompletedSection(
		val y: Int,
		val blocks: IntArray,
		val palette: Set<Pair<BlockState, CompoundTag?>>,
		val nmsPalette: ListTag
	)

	open fun complete(generator: SpaceGenerator): Deferred<Map<ChunkPos, Chunk>> {
		val asyncChunks = completedSectionMap.map { (chunkPos, _) ->
			generator.serverLevel.world.getChunkAtAsync(chunkPos.x, chunkPos.z)
		}.toTypedArray()

		val chunks = CompletableDeferred<Map<ChunkPos, Chunk>>()

		CompletableFuture.allOf(*asyncChunks).thenAccept {
			val completedLevelChunks = asyncChunks.map {
				val levelChunk = (it.get() as CraftChunk).handle
				return@map levelChunk.pos to levelChunk
			}.toMap()

			for ((levelChunkPos, completedSections) in completedSectionMap) {
				val chunkMinX = levelChunkPos.x.shl(4)
				val chunkMinZ = levelChunkPos.z.shl(4)

				val levelChunk = completedLevelChunks[levelChunkPos]!!

				for (completedSection in completedSections) {
					val section = levelChunk.sections[completedSection.y]
					val palette = completedSection.palette
					val map = palette.associateBy { palette.indexOf(it) }

					val sectionMinY = section.bottomBlockY()

					var index = 0

					for (x in 0..15) {
						val worldX = x + chunkMinX

						for (z in 0..15) {
							val worldZ = z + chunkMinZ

							for (y in 0..15) {
								val worldY = sectionMinY + y

								val block = map[completedSection.blocks[index]]!!

								if (block.first.isAir) {
									index++
									continue
								}

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

								index++
							}
						}
					}
				}

				Heightmap.primeHeightmaps(levelChunk, Heightmap.Types.values().toSet())
				levelChunk.playerChunk?.broadcastChanges(levelChunk)
				levelChunk.isUnsaved = true
			}

			chunks.complete(completedLevelChunks.mapValues { (_, levelChunk) -> levelChunk.bukkitChunk })
		}

		return chunks
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun store(generator: SpaceGenerator, chunks: Deferred<Map<ChunkPos, Chunk>>) {
		chunks.invokeOnCompletion {
			val finishedBukkitChunks = chunks.getCompleted()

			for ((chunkPos, sectionList) in completedSectionMap) {
				val bukkitChunk = finishedBukkitChunks[chunkPos]!!
				val sections = ListTag()

				val existingStoredBlocks = BlockSerialization
					.readChunkCompoundTag(
						bukkitChunk,
						NamespacedKeys.STORED_CHUNK_BLOCKS
					)

				for (completedSection in sectionList) {
					val newBlocks = BlockSerialization
						.formatSection(
							completedSection.y,
							completedSection.blocks,
							completedSection.nmsPalette
						)

					sections.add(newBlocks)
				}

				// Format the new data
				val finishedChunk = BlockSerialization.formatChunk(
					sections,
					generator.spaceGenerationVersion
				)

				// Combine and overwrite old data with new
				val combined = existingStoredBlocks?.let {
					BlockSerialization.combineSectionBlockStorage(
						existingStoredBlocks,
						finishedChunk
					)
				} ?: finishedChunk

				val outputStream = ByteArrayOutputStream()
				NbtIo.writeCompressed(combined, outputStream)

				// Update PDCs
				bukkitChunk.persistentDataContainer.set(
					NamespacedKeys.STORED_CHUNK_BLOCKS,
					PersistentDataType.BYTE_ARRAY,
					outputStream.toByteArray()
				)

				bukkitChunk.persistentDataContainer.set(
					NamespacedKeys.SPACE_GEN_VERSION,
					PersistentDataType.BYTE,
					generator.spaceGenerationVersion
				)
			}
		}
	}
}
