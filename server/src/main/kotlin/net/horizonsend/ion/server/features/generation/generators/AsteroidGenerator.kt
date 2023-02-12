package net.horizonsend.ion.server.features.generation.generators

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.generation.BlockSerialization
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
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
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.craftbukkit.v1_19_R2.CraftChunk
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Random
import java.util.concurrent.CompletableFuture
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object AsteroidGenerator {
	const val asteroidGenerationVersion: Byte = 0
	private val oreMap: Map<String, BlockState> = Ion.configuration.asteroidConfig.ores.associate {
		it.material to Bukkit.createBlockData(it.material).nms
	}
	private val weightedOres = oreWeights()
	val timing = timing("Space Generation")
	private const val searchRadius = 1.25

	fun postGenerateAsteroid(
		serverLevel: ServerLevel,
		asteroid: Asteroid
	) {
		Tasks.async {
			timing.time {
				val random = Random(serverLevel.seed)
				// save some time
				val noise = SimplexOctaveGenerator(random, 1)
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
							val nmsChunk = (bukkitChunk as CraftChunk).handle
							val newSections = mutableListOf<CompoundTag>()

							val chunkMinX = nmsChunk.pos.x * 16
							val chunkMinZ = nmsChunk.pos.z * 16

							val chunkBlocksChanged = mutableListOf<BlockPos>()

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
													noise
												)

											if (
												(
													random.nextDouble(0.0, 1.0) <= Ion.configuration.asteroidConfig.oreRatio &&
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
								BlockSerialization.readChunkBlocks(bukkitChunk, NamespacedKeys.ASTEROIDS_DATA)

							val formattedSections = existingSerializedAsteroidData.getList("sections", 10) // list of CompoundTag (10)
							formattedSections.addAll(newSections)
							val storedChunkBlocks =
								BlockSerialization.formatChunk(formattedSections, asteroidGenerationVersion)
							val outputStream = ByteArrayOutputStream()
							NbtIo.writeCompressed(storedChunkBlocks, outputStream)
							// end data serialization

							// updating chunk information
							Heightmap.primeHeightmaps(nmsChunk, Heightmap.Types.values().toSet())
							nmsChunk.isUnsaved = true

							// needs to be synchronized or multiple asteroids in a chunk cause issues
							Tasks.sync {
								nmsChunk.bukkitChunk.persistentDataContainer.set(
									NamespacedKeys.ASTEROIDS_DATA,
									PersistentDataType.BYTE_ARRAY,
									outputStream.toByteArray()
								)

								// broadcast updates synchronously
								completableBlocksChanged.thenAccept { completed ->
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
	 * */
	private fun checkBlockPlacement(
		worldX: Double,
		worldY: Double,
		worldZ: Double,
		worldXSquared: Double,
		worldYSquared: Double,
		worldZSquared: Double,
		asteroid: Asteroid,
		noise: SimplexOctaveGenerator
	): BlockState? {
		noise.setScale(0.15)

		val weightedMaterials = asteroid.materialWeights()

		// Calculate a noise pattern with a minimum at zero, and a max peak of the size of the materials list.
		val paletteSample = (
			(
				(
					noise.noise(
						worldX,
						worldY,
						worldZ,
						1.0,
						1.0,
						true
					) + 1
					) / 2
				) * (weightedMaterials.size - 1)
			).roundToInt()

		// Weight the list by adding duplicate entries, then sample it for the material.
		val material = weightedMaterials[paletteSample]

		// Full noise is used as the radius of the asteroid, and it is offset by the noise of each block pos.
		var fullNoise = 0.0

		for (octave in 0..asteroid.octaves) {
			noise.setScale(0.015 * (octave + 1.0).pow(2.25))

			val offset = abs(
				noise.noise(worldX, worldY, worldZ, 0.0, 1.0, false)
			) * (asteroid.size / (octave + 1.0).pow(2.25))

			fullNoise += offset
		}

		fullNoise *= fullNoise

		// Continue if block is not inside any asteroid
		if (worldXSquared + worldYSquared + worldZSquared >= fullNoise) return null

		return material
	}

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

	fun generateRandomAsteroid(x: Int, y: Int, z: Int, random: Random): Asteroid {
		val noise = SimplexOctaveGenerator(random, 1)

		noise.setScale(0.15)

		val weightedPalette = paletteWeights()
		val paletteSample = random.nextInt(weightedPalette.size)

		val blockPalette: ServerConfiguration.AsteroidConfig.Palette = weightedPalette[paletteSample]

		val size = random.nextDouble(5.0, Ion.configuration.asteroidConfig.maxAsteroidSize)
		val octaves = floor(5 * 0.95.pow(size)).toInt().coerceAtLeast(1)

		return Asteroid(x, y, z, blockPalette, size, octaves)
	}

	fun parseDensity(world: ServerLevel, x: Double, y: Double, z: Double): Double {
		val densities = mutableSetOf<Double>()
		densities.add(Ion.configuration.asteroidConfig.baseAsteroidDensity)

		for (feature in Ion.configuration.asteroidConfig.features) {
			if (feature.origin.world != world.serverLevelData.levelName) continue

			if ((sqrt((x - feature.origin.x).pow(2) + (z - feature.origin.z).pow(2)) - feature.tubeSize).pow(2) + (y - feature.origin.y).pow(
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
	private fun paletteWeights(): List<ServerConfiguration.AsteroidConfig.Palette> {
		val weightedList = mutableListOf<ServerConfiguration.AsteroidConfig.Palette>()

		for (palette in Ion.configuration.asteroidConfig.blockPalettes) {
			for (occurrence in palette.weight downTo 0) {
				weightedList.add(palette)
			}
		}

		return weightedList
	}

	private fun oreWeights(): List<ServerConfiguration.AsteroidConfig.Ore> {
		val weightedList = mutableListOf<ServerConfiguration.AsteroidConfig.Ore>()

		for (ore in Ion.configuration.asteroidConfig.ores) {
			for (occurrence in ore.rolls downTo 0) {
				weightedList.add(ore)
			}
		}

		return weightedList
	}

	data class Asteroid(
		val x: Int,
		val y: Int,
		val z: Int,
		val palette: ServerConfiguration.AsteroidConfig.Palette,
		val size: Double,
		val octaves: Int
	) {
		fun materialWeights(): List<BlockState> {
			val weightedList = mutableListOf<BlockState>()

			for (material in palette.materials) {
				for (occurrence in material.value downTo 0) {
					weightedList.add(palette.getMaterial(material.key).nms)
				}
			}

			return weightedList
		}
	}
}
