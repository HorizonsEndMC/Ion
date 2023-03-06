package net.horizonsend.ion.server.features.space.generation.generators

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import kotlinx.coroutines.CompletableDeferred
import net.horizonsend.ion.server.features.space.generation.BlockSerialization
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.Heightmap
import net.starlegacy.util.Tasks
import net.starlegacy.util.nms
import net.starlegacy.util.toBukkitBlockData
import net.starlegacy.util.worldEditSession
import org.bukkit.Chunk
import org.bukkit.craftbukkit.v1_19_R2.CraftChunk
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture

class GenerateWreckTask(
	override val generator: SpaceGenerator,
	private val wreck: SpaceGenerator.WreckGenerationData
) : SpaceGenerationTask<WreckGenerationData.WreckReturnData>() {
	val serverLevel = generator.serverLevel
	override val returnData = CompletableDeferred<WreckGenerationData.WreckReturnData>()

	override fun generate() {
		val encounter = wreck.encounter?.getEncounter()
		Tasks.async {
			val schematic: Clipboard = generator.schematicMap[wreck.schematicName]!!

			serverLevel.world.worldEditSession(true) {
				val region = schematic.region.clone()
				val targetBlockVector = BlockVector3.at(wreck.x, wreck.y, wreck.z)
				val offset = targetBlockVector.subtract(schematic.origin)

				region.shift(offset)

				val sectionsSet = mutableSetOf<Int>()
				val wreckChest = CompletableFuture<BlockPos>()

				for (
				y in (region.boundingBox.minimumY - serverLevel.minBuildHeight)..(region.boundingBox.maximumY - serverLevel.minBuildHeight)
				) {
					sectionsSet.add(y.shr(4))
				}

				for (chunkPosWE in region.chunks) {
					val chunkOriginX = chunkPosWE.x * 16
					val chunkOriginZ = chunkPosWE.z * 16

					serverLevel.world.getChunkAtAsync(
						chunkPosWE.x,
						chunkPosWE.z
					).thenAcceptAsync completedChunk@{ bukkitChunk ->
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

							var index = 0

							for (x in 0..15) {
								val worldX = x + chunkOriginX

								for (z in 0..15) {
									val worldZ = z + chunkOriginZ

									for (y in 0..15) {
										val worldY = bottomY + y
										val schematicRelative = BlockVector3.at(worldX, worldY, worldZ).subtract(offset)

										val baseBlock = schematic.getFullBlock(schematicRelative)
										var blockState: BlockState = baseBlock.toImmutableState().toBukkitBlockData().nms
										val blockNBT = if (blockState.hasBlockEntity()) baseBlock.nbtData else null

										val existingBlock = levelChunkSection.getBlockState(x, y, z)

										if (!existingBlock.isAir) {
											palette.add(existingBlock)
											storedBlocks[index] = palette.indexOf(existingBlock)
											index++
											continue
										}

										if (blockState.isAir) {
											storedBlocks[index] = 0
											index++
											continue
										}

										encounter?.let {
											blockNBT?.let {
												if (blockState.block == Blocks.CHEST &&
													blockNBT.getString("CustomName").contains(
															"Encounter Chest",
															true
														)
												) {
													wreckChest.complete(BlockPos(worldX, worldY, worldZ))
													blockState = encounter.constructChestState()
												}
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

							val paletteListTag = ListTag()
							palette.mapTo(paletteListTag) { blockState -> NbtUtils.writeBlockState(blockState) }.toList()

							// Add the stored blocks to the list of sections
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

						val completableBlockData = CompletableFuture<ByteArray>()

						// Check chunk for existing spacegen blocks
						val existingChunkBlocks =
							BlockSerialization.readChunkCompoundTag(
								bukkitChunk,
								NamespacedKeys.STORED_CHUNK_BLOCKS
							)

						// list of CompoundTag (10)
						val formattedSections = existingChunkBlocks.getList("sections", 10)
						formattedSections.addAll(newSections)

						val blocksOutputStream = ByteArrayOutputStream()
						NbtIo.writeCompressed(
							BlockSerialization.formatChunk(formattedSections, generator.spaceGenerationVersion),
							blocksOutputStream
						)

						completableBlockData.complete(blocksOutputStream.toByteArray())
						// End wreck block storage

						// updating chunk information
						Heightmap.primeHeightmaps(levelChunk, Heightmap.Types.values().toSet())
						levelChunk.isUnsaved = true

						// Write wreck data to chunk
						wreckChest.thenAccept { chestPos ->
							Tasks.sync {
								val existingWrecksBaseTag = BlockSerialization
									.readChunkCompoundTag(
										levelChunk.bukkitChunk,
										NamespacedKeys.WRECK_ENCOUNTER_DATA
									)
								val existingWrecks =
									existingWrecksBaseTag.getList("wrecks", 10) // list of compound tags (10)

								// Won't be null if the encounter is not null, unless someone messed up
								wreck.encounter?.let {
									existingWrecks += it.nms(
										chestPos.x,
										chestPos.y,
										chestPos.z
									)
								}

								val newFinishedData = CompoundTag()
								newFinishedData.put("wrecks", existingWrecks)
								val wreckDataOutputStream = ByteArrayOutputStream()
								NbtIo.writeCompressed(newFinishedData, wreckDataOutputStream)

								levelChunk.bukkitChunk.persistentDataContainer.set(
									NamespacedKeys.WRECK_ENCOUNTER_DATA,
									PersistentDataType.BYTE_ARRAY,
									wreckDataOutputStream.toByteArray()
								)
							}
						}

						completableBlockData.thenAccept {
							Tasks.sync {
								levelChunk.bukkitChunk.persistentDataContainer.set(
									NamespacedKeys.STORED_CHUNK_BLOCKS,
									PersistentDataType.BYTE_ARRAY,
									blocksOutputStream.toByteArray()
								)
							}
						}

						// needs to be synchronized or multiple asteroids in a chunk cause issues
						completableBlocksChanged.thenAccept { completed ->
							Tasks.sync {
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
	}
}

/**
 * This class contains information passed to the generation function.
 * @param [x, y ,z] Origin of the asteroid.
 * @param wreckName Name of the wreck schematic
 * @param encounterIdentifier Wreck encounter identifier
 **/
data class WreckGenerationData(
	override val x: Int,
	override val y: Int,
	override val z: Int,
	val wreckName: String,
	val encounterIdentifier: String? = null
) : SpaceGenerationData() {

	data class WreckReturnData(
		override val completedSectionMap: Map<LevelChunk, List<CompletedSection>>,
		val serializedWreckData: Pair<Chunk, CompoundTag>? = null,
		val callback: () -> Unit = {}
	) : SpaceGenerationReturnData() {
		override fun complete(generator: SpaceGenerator) {
			TODO("Not yet implemented")
		}
	}
}
