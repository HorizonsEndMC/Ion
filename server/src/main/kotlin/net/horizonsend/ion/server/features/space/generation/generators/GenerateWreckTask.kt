package net.horizonsend.ion.server.features.space.generation.generators

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.encounters.Encounter
import net.horizonsend.ion.server.features.space.encounters.Encounters
import net.horizonsend.ion.server.features.space.encounters.SecondaryChests
import net.horizonsend.ion.server.features.space.generation.BlockSerialization
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.starlegacy.util.nms
import net.starlegacy.util.toBukkitBlockData
import net.starlegacy.util.worldEditSession
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class GenerateWreckTask(
	override val generator: SpaceGenerator,
	override val chunk: ChunkPos,
	val chunkCoveredWrecks: List<WreckGenerationData>
) : SpaceGenerationTask<WreckGenerationData.WreckReturnData>() {
	val serverLevel = generator.serverLevel

	override val returnData = CompletableDeferred<WreckGenerationData.WreckReturnData>()

	var encounterPrimaryChest: Pair<ChunkPos, BlockPos>? = null
	private val secondaryChests: MutableList<CompoundTag> = mutableListOf()

	override suspend fun generateChunk(scope: CoroutineScope) {
		val chunkCompletedSections = mutableListOf<SpaceGenerationReturnData.CompletedSection>()
		scope.launch {
			for (wreck in chunkCoveredWrecks) {
				val clipboard: Clipboard = generator.schematicMap[wreck.wreckName]!!

				val region = clipboard.region.clone()
				val targetBlockVector: BlockVector3 = BlockVector3.at(wreck.x, wreck.y, wreck.z)
				val offset: BlockVector3 = targetBlockVector.subtract(clipboard.origin)

				val encounter = wreck.encounter?.getEncounter()

				serverLevel.world.worldEditSession(true) {
					region.shift(offset)

					val sectionsSet = mutableSetOf<Int>()

					for (
					y in (region.boundingBox.minimumY - serverLevel.minBuildHeight)..(region.boundingBox.maximumY - serverLevel.minBuildHeight)
					) {
						sectionsSet.add(y.shr(4))
					}

					val chunkOriginX = chunk.x * 16
					val chunkOriginZ = chunk.z * 16

					for (sectionPos in sectionsSet) {
						val newlyCompleted = generateSection(
							clipboard,
							offset,
							sectionPos,
							chunkOriginX,
							chunkOriginZ,
							chunk,
							encounter
						) ?: continue

						chunkCompletedSections.add(newlyCompleted)
					}

					val serializedWreckData: CompoundTag? = encounterPrimaryChest?.let { chestPos ->
						return@let wreck.encounter!!.nms(
							chestPos.second.x,
							chestPos.second.y,
							chestPos.second.z
						)
					}

					returnData.complete(
						WreckGenerationData.WreckReturnData(
							chunkCompletedSections,
							serializedWreckData,
							secondaryChests
						)
					)
				}
			}
		}
	}

	private fun generateSection(
		clipboard: Clipboard,
		offset: BlockVector3,
		sectionY: Int,
		chunkMinX: Int,
		chunkMinZ: Int,
		chunkPos: ChunkPos,
		encounter: Encounter?
	): SpaceGenerationReturnData.CompletedSection? {
		val palette = mutableListOf<Pair<BlockState, CompoundTag?>>()
		val storedBlocks = IntArray(4096)
		val sectionMinY = sectionY.shl(4)

		palette.add(Blocks.AIR.defaultBlockState() to null)
		val paletteListTag = ListTag()

		for (x in 0..15) {
			val worldX = x + chunkMinX

			for (z in 0..15) {
				val worldZ = z + chunkMinZ

				for (y in 0..15) {
					val worldY = sectionMinY + y
					val schematicRelative = BlockVector3.at(worldX, worldY, worldZ).subtract(offset)

					val baseBlock = clipboard.getFullBlock(schematicRelative)
					val originalBlockState: BlockState = baseBlock.toImmutableState().toBukkitBlockData().nms
					val blockNBT = if (originalBlockState.hasBlockEntity()) baseBlock.nbtData else null

					val index = BlockSerialization.posToIndex(x, y, z)

					if (originalBlockState.isAir) {
						storedBlocks[index] = 0
						continue
					}

					var combined = originalBlockState to blockNBT?.nms()

					blockNBT?.let blockNBT@{
						val name = blockNBT.getString("CustomName")

						encounter?.let encounter@{ encounter ->
							if (originalBlockState.block != Blocks.CHEST) return@encounter
							if (!name.contains("Encounter Chest", true)) return@encounter

							encounterPrimaryChest = chunkPos to BlockPos(worldX, worldY, worldZ)
							combined = encounter.constructChestState()
						}

						// Use let to be able to exit out of the statement
						if (name.contains("Secondary: ", ignoreCase = true)) {
							let secondaryChest@{
								val chestType = name.substringAfter("Secondary: ").substringBefore("\"")

								val secondaryChest = SecondaryChests[chestType] ?: return@secondaryChest

								combined = secondaryChest.blockState to secondaryChest.NBT

								val serialized = CompoundTag()

								serialized.putInt("x", worldX)
								serialized.putInt("y", worldY)
								serialized.putInt("z", worldZ)

								secondaryChest.money?.let { money -> serialized.putInt("Money", money) }

								secondaryChests.add(serialized)
							}
						}
					}

					// Format the block entity
					(combined.second)?.putInt("x", worldX)
					(combined.second)?.putInt("y", worldY)
					(combined.second)?.putInt("z", worldZ)

					val blockIndex = if (!palette.contains(combined)) {
						palette.add(combined)
						palette.lastIndex
					} else palette.indexOf(combined)

					storedBlocks[index] = blockIndex
				}
			}
		}

		if (storedBlocks.all { it == 0 }) return null // don't write it if it's all empty

		palette.forEach { blockState ->
			val base = NbtUtils.writeBlockState(blockState.first)
			blockState.second?.let { base.put("TileEntity", it) }
			paletteListTag.add(base)
		}

		return SpaceGenerationReturnData.CompletedSection(
			sectionY,
			storedBlocks,
			palette,
			paletteListTag
		)
	}
}

/**
 * This class contains information passed to the generation function.
 * @param [x, y ,z] Origin of the asteroid.
 * @param wreckName Name of the wreck schematic
 * @param encounter Wreck encounter identifier
 **/
data class WreckGenerationData(
	override val x: Int,
	override val y: Int,
	override val z: Int,
	val wreckName: String,
	val encounter: WreckEncounterData? = null
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
		fun getEncounter(): Encounter = Encounters[identifier]!!

		fun nms(x: Int, y: Int, z: Int): CompoundTag {
			val beginningTag = CompoundTag()

			beginningTag.putInt("x", x)
			beginningTag.putInt("y", y)
			beginningTag.putInt("z", z)

			beginningTag.putString("Encounter Identifier", identifier)

			return beginningTag
		}
	}

	data class WreckReturnData(
		override val completedSectionMap: List<CompletedSection>,
		val serializedWreckData: CompoundTag? = null,
		val secondaryChests: List<CompoundTag>
	) : SpaceGenerationReturnData() {
		@OptIn(ExperimentalCoroutinesApi::class)
		override fun finishPlacement(chunk: ChunkPos, generator: SpaceGenerator): Deferred<LevelChunk> {
			val acquiredChunk = super.finishPlacement(chunk, generator)

			acquiredChunk.invokeOnCompletion {
				val finishedChunk = acquiredChunk.getCompleted().bukkitChunk

				serializedWreckData?.let { data ->
					// It is most definitely inside the covered chunks
					val existingWrecksBaseTag = BlockSerialization
						.readChunkCompoundTag(
							finishedChunk,
							NamespacedKeys.WRECK_ENCOUNTER_DATA
						)

					// list of compound tags (10)
					val existingWrecks = existingWrecksBaseTag?.getList("Wrecks", 10) ?: ListTag()

					existingWrecks.add(data)

					val newFinishedData = CompoundTag()

					newFinishedData.put("Wrecks", existingWrecks)

					val byteArray = ByteArrayOutputStream()

					val dataOutput = DataOutputStream(byteArray)
					NbtIo.write(newFinishedData, dataOutput)

					// Update PDCs
					finishedChunk.persistentDataContainer.set(
						NamespacedKeys.WRECK_ENCOUNTER_DATA,
						PersistentDataType.BYTE_ARRAY,
						byteArray.toByteArray()
					)
				}

				for (chest in secondaryChests) {
					val existingWrecksBaseTag = BlockSerialization
						.readChunkCompoundTag(
							finishedChunk,
							NamespacedKeys.WRECK_ENCOUNTER_DATA
						)

					// list of compound tags (10)
					val existingChests = existingWrecksBaseTag?.getList("SecondaryChests", 10) ?: ListTag()
					val existingWrecks = existingWrecksBaseTag?.getList("Wrecks", 10) ?: ListTag()

					existingChests.add(chest)

					val newFinishedData = CompoundTag()

					newFinishedData.put("SecondaryChests", existingChests)
					newFinishedData.put("Wrecks", existingWrecks)

					val byteArray = ByteArrayOutputStream()

					val dataOutput = DataOutputStream(byteArray)
					NbtIo.write(newFinishedData, dataOutput)

					// Update PDCs
					finishedChunk.persistentDataContainer.set(
						NamespacedKeys.WRECK_ENCOUNTER_DATA,
						PersistentDataType.BYTE_ARRAY,
						byteArray.toByteArray()
					)
				}
			}

			return acquiredChunk
		}
	}
}
