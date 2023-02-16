package net.horizonsend.ion.server.features.space.generation.generators

import com.sk89q.jnbt.NBTInputStream
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.SpongeSchematicReader
import com.sk89q.worldedit.math.BlockVector3
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.space.encounters.Encounter
import net.horizonsend.ion.server.features.space.encounters.Encounters
import net.horizonsend.ion.server.features.space.encounters.Encounters.ITS_A_TRAP
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
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.starlegacy.util.Tasks
import net.starlegacy.util.nms
import net.starlegacy.util.time
import net.starlegacy.util.timing
import net.starlegacy.util.toBukkitBlockData
import net.starlegacy.util.worldEditSession
import org.bukkit.Bukkit
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
import kotlin.math.abs
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

	/**
	 * This class contains information passed to the generation function.
	 * @param [x, y ,z] Origin of the asteroid.
	 * @param palette A weighted list of blocks.
	 * @param size The radius of the asteroid before noise deformation.
	 * @param octaves The number of octaves of noise to apply. Generally 1, but higher for small asteroids. Increases roughness.
	 **/
	data class AsteroidGenerationData(
		val x: Int,
		val y: Int,
		val z: Int,
		val palette: WeightedRandomList<BlockState>,
		val size: Double,
		val octaves: Int
	)

	// ASTEROIDS SECTION
	private val oreMap: Map<String, BlockState> = configuration.ores.associate {
		it.material to Bukkit.createBlockData(it.material).nms
	}

	// Palettes weighted
	val weightedPalettes = configuration.paletteWeightedList()

// 	private val weightedPalettes = configuration.blockPalettes.associateWith { it.materialWeights() }
	private val weightedOres = oreWeights()

	// Multiple of the radius of the asteroid to mark chunks as might contain an asteroid
	private val searchRadius = 1.25

	fun generateAsteroid(
		asteroid: AsteroidGenerationData
	) {
		timing.time {
			Tasks.async {
				// save some time
				val sizeFactor = asteroid.size / 15
				val shapingNoise = SimplexOctaveGenerator(random, 1)
				val materialNoise = SimplexOctaveGenerator(random, 1)
				materialNoise.setScale(0.15 / (sizeFactor * 2))

				val radiusSquared = asteroid.size * asteroid.size

				// Get every chunk section covered by the asteroid.
				val coveredChunks = mutableMapOf<ChunkPos, List<Int>>()

				// generate ranges ahead of time
				val xRange =
					IntRange(asteroid.x - (asteroid.size * searchRadius).toInt(), asteroid.x + (asteroid.size * searchRadius).toInt())
				val zRange =
					IntRange(asteroid.z - (asteroid.size * searchRadius).toInt(), asteroid.z + (asteroid.size * searchRadius).toInt())
				val yRange = IntRange(
					(asteroid.y - (asteroid.size * searchRadius).toInt()).coerceAtLeast(serverLevel.minBuildHeight),
					(asteroid.y + (asteroid.size * searchRadius).toInt()).coerceAtMost(serverLevel.maxBuildHeight)
				)

				val chunkXRange = IntRange(xRange.first.shr(4), xRange.last.shr(4))
				val chunkZRange = IntRange(zRange.first.shr(4), zRange.last.shr(4))
				val chunkYRange = IntRange(yRange.first.shr(4), yRange.last.shr(4) + serverLevel.minBuildHeight.shr(4))

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
					serverLevel.world.getChunkAtAsync(nmsChunkPos.x, nmsChunkPos.z)
						.thenAcceptAsync completedChunk@{ bukkitChunk ->
							val completableBlocksChanged: CompletableFuture<List<BlockPos>> = CompletableFuture()
							val chunkBlocksChanged = mutableListOf<BlockPos>()

							val nmsChunk = (bukkitChunk as CraftChunk).handle
							val newSections = mutableListOf<CompoundTag>()

							val chunkMinX = nmsChunk.pos.x.shl(4)
							val chunkMinZ = nmsChunk.pos.z.shl(4)

							for (sectionPos in sectionList) {
								val section = nmsChunk.sections[sectionPos]

								val palette = mutableSetOf<BlockState>()
								val storedBlocks = arrayOfNulls<Int>(4096)
								var index = 0
								val sectionMinY = section.bottomBlockY()

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
													zSquared,
													asteroid,
													sizeFactor,
													shapingNoise,
													materialNoise
												)

											if (
												(
													random.nextDouble(0.0, 1.0) <= configuration.oreRatio &&
														block != null
													) && !block.isAir
											) {
												val ore = weightedOres[random.nextInt(0, weightedOres.size - 1)]
												block = oreMap[ore.material]
											}

											if (block != null) {
												palette.add(block)
												storedBlocks[index] = palette.indexOf(block)

												section.setBlockState(
													x,
													y,
													z,
													block
												)
											} else {
												storedBlocks[index] = 0
											}

											// needs to be run on main thread
											chunkBlocksChanged += BlockPos(worldX, worldY, worldZ)

											index++
										}
									}
								}

								if (storedBlocks.all { it == 0 }) continue // don't write it if it's all empty

								palette.forEach { blockState -> paletteListTag.add(NbtUtils.writeBlockState(blockState)) }

								val intArray = storedBlocks.requireNoNulls().toIntArray()
								newSections += BlockSerialization.formatSection(
									sectionPos,
									intArray,
									paletteListTag
								)
							}
							// start broadcasting the chunk information
							completableBlocksChanged.complete(chunkBlocksChanged)

							// Chunk is empty, everything else is unnecessary
							if (newSections.isEmpty()) return@completedChunk

							// data serialization
							val existingSerializedAsteroidData =
								BlockSerialization.readChunkCompoundTag(bukkitChunk, NamespacedKeys.ASTEROIDS_DATA)

							val formattedSections = existingSerializedAsteroidData.getList("sections", 10) // list of CompoundTag (10)
							formattedSections.addAll(newSections)
							val storedChunkBlocks =
								BlockSerialization.formatChunk(formattedSections, spaceGenerationVersion)
							val outputStream = ByteArrayOutputStream()
							NbtIo.writeCompressed(storedChunkBlocks, outputStream)
							// end data serialization

							// updating chunk information
							Heightmap.primeHeightmaps(nmsChunk, Heightmap.Types.values().toSet())
							nmsChunk.isUnsaved = true

							// needs to be synchronized or multiple asteroids in a chunk cause issues
							completableBlocksChanged.thenAccept { completed ->
								Tasks.sync {
									nmsChunk.bukkitChunk.persistentDataContainer.set(
										NamespacedKeys.ASTEROIDS_DATA,
										PersistentDataType.BYTE_ARRAY,
										outputStream.toByteArray()
									)

									// broadcast updates synchronously
									for (blockPos in completed) {
										nmsChunk.playerChunk?.blockChanged(blockPos)
									}

									nmsChunk.playerChunk?.broadcastChanges(nmsChunk)
								}
							}
							// end chunk update
						}
				}
			}
		}
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
		worldZSquared: Double,
		asteroid: AsteroidGenerationData,
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

	fun generateRandomAsteroid(x: Int, y: Int, z: Int, random: Random): AsteroidGenerationData {
		val blockPalette: WeightedRandomList<BlockState> = weightedPalettes.random()

		val size = random.nextDouble(10.0, configuration.maxAsteroidSize)
		val octaves = floor(3 * 0.998.pow(size)).toInt().coerceAtLeast(1)

		return AsteroidGenerationData(x, y, z, blockPalette, size, octaves)
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
	val weightedWreckList = WeightedRandomList<String>().apply { this.addMany(configuration.wrecks) }

	fun generateWreck(wreck: WreckGenerationData) {
		val encounter = wreck.encounter?.getEncounter()
		Tasks.async {
			val schematic = wreck.schematic()

			serverLevel.world.worldEditSession(true) {
				val region = schematic.region.clone()
				val targetBlockVector = BlockVector3.at(wreck.x, wreck.y, wreck.z)
				val offset = targetBlockVector.subtract(schematic.origin)

				region.shift(offset)
				val sectionsSet = mutableSetOf<Int>()

				for (
				y in (region.boundingBox.minimumY - serverLevel.minBuildHeight)..(region.boundingBox.maximumY - serverLevel.minBuildHeight)
				) {
					sectionsSet.add(y.shr(4))
				}

				for (chunkPosWE in region.chunks) {
					val chunkOriginX = chunkPosWE.x * 16
					val chunkOriginZ = chunkPosWE.z * 16

					serverLevel.world.getChunkAtAsync(chunkPosWE.x, chunkPosWE.z)
						.thenAcceptAsync completedChunk@{ bukkitChunk ->
							val completableBlocksChanged: CompletableFuture<List<BlockPos>> = CompletableFuture()
							val chunkBlocksChanged = mutableListOf<BlockPos>()

							val levelChunk = (bukkitChunk as CraftChunk).handle
							val newSections = mutableListOf<CompoundTag>()

							for (section in sectionsSet) {
								val levelChunkSection = levelChunk.sections[section]
								val bottomY = levelChunkSection.bottomBlockY()

								val palette = mutableSetOf<BlockState>()
								palette.add(Blocks.AIR.defaultBlockState())
								val storedBlocks = arrayOfNulls<Int>(4096)
								val paletteListTag = ListTag()

								var index = 0

								for (x in 0..15) {
									val worldX = x + chunkOriginX

									for (z in 0..15) {
										val worldZ = z + chunkOriginZ

										for (y in 0..15) {
											val worldY = bottomY + y
											val schematicRelative = BlockVector3.at(worldX, worldY, worldZ).subtract(offset)

											val baseBlock = schematic.getFullBlock(schematicRelative)
											var blockState = baseBlock.toImmutableState().toBukkitBlockData().nms

											if (blockState.isAir) {
												storedBlocks[index] = 0
												index++
												continue
											}

											if (encounter != null) {
												if (
													wreck.encounter.chestX == schematicRelative.blockX &&
													wreck.encounter.chestX == schematicRelative.blockX &&
													wreck.encounter.chestX == schematicRelative.blockX
												) {
													blockState = encounter.constructChestState()
												}
											}

											// needs to be run on main thread
											chunkBlocksChanged += BlockPos(worldX, worldY, worldZ)

											palette.add(blockState)
											storedBlocks[index] = palette.indexOf(blockState)
											levelChunkSection.setBlockState(
												x,
												y,
												z,
												blockState
											)

											index++
										}
									}
								}

								if (storedBlocks.all { it == 0 }) continue
								 // don't write it if it's all empty

								palette.forEach { blockState -> paletteListTag.add(NbtUtils.writeBlockState(blockState)) }

								val intArray = storedBlocks.requireNoNulls().toIntArray()
								newSections += BlockSerialization.formatSection(
									section,
									intArray,
									paletteListTag
								)
							}

							// start broadcasting the chunk information
							completableBlocksChanged.complete(chunkBlocksChanged)

							// Chunk is empty, everything else is unnecessary
							if (newSections.isEmpty()) return@completedChunk

							// Wreck block storage
							val existingSerializedAsteroidData =
								BlockSerialization.readChunkCompoundTag(bukkitChunk, NamespacedKeys.ASTEROIDS_DATA)

							val formattedSections = existingSerializedAsteroidData.getList("sections", 10) // list of CompoundTag (10)
							formattedSections.addAll(newSections)
							val storedChunkBlocks =
								BlockSerialization.formatChunk(formattedSections, spaceGenerationVersion)
							val wreckBlocksOutputStream = ByteArrayOutputStream()
							NbtIo.writeCompressed(storedChunkBlocks, wreckBlocksOutputStream)
							// end data serialization

							// updating chunk information
							Heightmap.primeHeightmaps(levelChunk, Heightmap.Types.values().toSet())
							levelChunk.isUnsaved = true
							// End wreck block storage

							// Wreck data
							val existingWrecksBaseTag = BlockSerialization.readChunkCompoundTag(levelChunk.bukkitChunk, NamespacedKeys.WRECK_DATA)
							val existingWrecks = existingWrecksBaseTag.getList("wrecks", 10) // list of compound tags (10)

							wreck.encounter?.let {
								existingWrecks += it.NMS(
									wreck.x,
									wreck.y, wreck.z
								) // TODO better way of getting chest pos
							}

							val newFinishedData = CompoundTag()
							newFinishedData.put("wrecks", existingWrecks)
							val wreckDataOutputStream = ByteArrayOutputStream()
							NbtIo.writeCompressed(newFinishedData, wreckDataOutputStream)

							// needs to be synchronized or multiple asteroids in a chunk cause issues
							completableBlocksChanged.thenAccept { completed ->
								Tasks.sync {
									levelChunk.bukkitChunk.persistentDataContainer.set(
										NamespacedKeys.ASTEROIDS_DATA,
										PersistentDataType.BYTE_ARRAY,
										wreckBlocksOutputStream.toByteArray()
									)

									levelChunk.bukkitChunk.persistentDataContainer.set(
										NamespacedKeys.WRECK_DATA,
										PersistentDataType.BYTE_ARRAY,
										wreckDataOutputStream.toByteArray()
									)

									// broadcast updates synchronously
									for (blockPos in completed) {
										levelChunk.playerChunk?.blockChanged(blockPos)
									}

									levelChunk.playerChunk?.broadcastChanges(levelChunk)
								}
							}
						}
				}
			}
		}

		val chunk = serverLevel.world.getChunkAt(0, 0) // placeholder

		val existingWrecksBaseTag = BlockSerialization.readChunkCompoundTag(chunk, NamespacedKeys.ASTEROIDS_DATA)
		val existingWrecks = existingWrecksBaseTag.getList("wrecks", 10) // list of compound tags (10)

		// wreck.encounter?.NMS()?.let { existingWrecks += it }

		val newFinishedData = CompoundTag()
		newFinishedData.put("wrecks", existingWrecks)
		val outputStream = ByteArrayOutputStream()
		NbtIo.writeCompressed(newFinishedData, outputStream)

		chunk.persistentDataContainer.set(
			NamespacedKeys.WRECK_DATA,
			PersistentDataType.BYTE_ARRAY,
			outputStream.toByteArray()
		)
	}

	/**
	 * This information is not serialized. It is used in the generation of the wreck.
	 * @param schematicName The name of the schematic file referenced, not including file extension
	 * @param encounter The optional encounter data.
	 **/
	data class WreckGenerationData(
		val x: Int,
		val y: Int,
		val z: Int,
		val schematicName: String,
		val encounter: WreckEncounterData?
	) {
		fun schematic(): Clipboard {
			val file: File = Ion.dataFolder.resolve("wrecks").resolve("$schematicName.schem")
// 			val compoundTag = NbtIo.readCompressed(FileInputStream(file))
// 			encounter?.let { compoundTag.put("encounter", it.NMS()) }

			return SpongeSchematicReader(NBTInputStream(GZIPInputStream(FileInputStream(file)))).read()
		}

		/**
		 * This is serialized and stored in the chunk alongside the wreck.
		 *
		 * @param identifier The identifier string for the encounter class
		 **/
		data class WreckEncounterData(
			val chestX: Int,
			val chestY: Int,
			val chestZ: Int,
			val identifier: String
		) {
			fun getEncounter(): Encounter = Encounters.getByIdentifier(identifier)!!

			fun NMS(x: Int, y: Int, z: Int): CompoundTag {
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
		return WreckGenerationData(
			x,
			y,
			z,
			weightedWreckList.random(),
			WreckGenerationData.WreckEncounterData(
				0,
				0,
				0,
				ITS_A_TRAP.identifier
			)
		)
	}

	companion object {
		fun rebuildChunkAsteroids(chunk: Chunk) {
			val storedAsteroidData =
				chunk.persistentDataContainer.get(NamespacedKeys.ASTEROIDS_DATA, PersistentDataType.BYTE_ARRAY)
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
