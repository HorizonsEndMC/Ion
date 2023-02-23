package net.horizonsend.ion.server.features.space.generation.generators

import net.horizonsend.ion.server.features.space.generation.BlockSerialization
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationTask
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.starlegacy.util.Tasks
import net.starlegacy.util.time
import org.bukkit.craftbukkit.v1_19_R2.CraftChunk
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class GenerateAsteroidTask(
	override val generator: SpaceGenerator,
	private val asteroid: SpaceGenerator.AsteroidGenerationData
) : SpaceGenerationTask() {
	val config = generator.configuration

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
		asteroid: SpaceGenerator.AsteroidGenerationData,
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

	override fun run() {
		generator.timing.time {
			Tasks.async {
				// save some time
				val sizeFactor = asteroid.size / 15
				val shapingNoise = SimplexOctaveGenerator(generator.random, 1)
				val materialNoise = SimplexOctaveGenerator(generator.random, 1)
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
					while (SpaceGenerationManager.chunkLock.contains(nmsChunkPos)) {
						Thread.sleep(1)
					}

					generator.serverLevel.world.getChunkAtAsync(nmsChunkPos.x, nmsChunkPos.z)
						.thenAcceptAsync completedChunk@{ bukkitChunk ->
							val completableBlocksChanged: CompletableFuture<List<BlockPos>> = CompletableFuture()
							val chunkBlocksChanged = mutableListOf<BlockPos>()
							SpaceGenerationManager.chunkLock.add(nmsChunkPos)

							val levelChunk = (bukkitChunk as CraftChunk).handle
							val newSections = mutableListOf<CompoundTag>()

							val chunkMinX = levelChunk.pos.x.shl(4)
							val chunkMinZ = levelChunk.pos.z.shl(4)

							for (sectionPos in sectionList) {
								val levelChunkSection = levelChunk.sections[sectionPos]

								val palette = mutableSetOf<BlockState>()
								val storedBlocks = arrayOfNulls<Int>(4096)
								var index = 0
								val sectionMinY = levelChunkSection.bottomBlockY()

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

											val existingBlock = levelChunkSection.getBlockState(x, y, z)

											if (!existingBlock.isAir) {
												palette.add(existingBlock)
												storedBlocks[index] = palette.indexOf(existingBlock)
												index++
												continue
											}

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

												levelChunkSection.setBlockState(
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
							val completableBlockData = CompletableFuture<ByteArray>()
							val existingSerializedAsteroidData =
								BlockSerialization.readChunkCompoundTag(bukkitChunk, NamespacedKeys.STORED_CHUNK_BLOCKS)

							val formattedSections = existingSerializedAsteroidData.getList("sections", 10) // list of CompoundTag (10)
							formattedSections.addAll(newSections)
							val storedChunkBlocks =
								BlockSerialization.formatChunk(formattedSections, generator.spaceGenerationVersion)
							val outputStream = ByteArrayOutputStream()
							NbtIo.writeCompressed(storedChunkBlocks, outputStream)
							completableBlockData.complete(outputStream.toByteArray())
							// end data serialization

							// updating chunk information
							Heightmap.primeHeightmaps(levelChunk, Heightmap.Types.values().toSet())
							levelChunk.isUnsaved = true

							// needs to be synchronized or multiple asteroids in a chunk cause issues
							completableBlockData.thenAccept { blockData ->
								// Write PDC sync to avoid issues
								Tasks.sync {
									bukkitChunk.persistentDataContainer.set(
										NamespacedKeys.STORED_CHUNK_BLOCKS,
										PersistentDataType.BYTE_ARRAY,
										blockData
									)
								}
							}

							completableBlocksChanged.thenAccept { completed ->
								Tasks.sync {
									// broadcast updates synchronously
									for (blockPos in completed) {
										levelChunk.playerChunk?.blockChanged(blockPos)
									}

									levelChunk.playerChunk?.broadcastChanges(levelChunk)
								}
							}
							// end chunk update

							SpaceGenerationManager.chunkLock.remove(nmsChunkPos)
						}
				}
			}
		}
	}
}
