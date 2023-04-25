package net.horizonsend.ion.server.features.space.generation.generators

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.data.BlockData
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks
import net.horizonsend.ion.server.features.space.encounters.Encounter
import net.horizonsend.ion.server.features.space.encounters.Encounters
import net.horizonsend.ion.server.features.space.encounters.SecondaryChest
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.ENCOUNTER
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.SECONDARY_CHEST
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.starlegacy.util.nms
import net.starlegacy.util.toBukkitBlockData
import net.starlegacy.util.worldEditSession
import org.bukkit.craftbukkit.v1_19_R2.persistence.CraftPersistentDataContainer
import org.bukkit.craftbukkit.v1_19_R2.persistence.CraftPersistentDataTypeRegistry
import org.bukkit.persistence.PersistentDataType.STRING

class GenerateWreckTask(
	override val generator: SpaceGenerator,
	override val chunk: LevelChunk,
	val chunkCoveredWrecks: List<WreckGenerationData>
) : SpaceGenerationTask() {
	val serverLevel = generator.serverLevel

	override val returnData = CompletableDeferred<StoredChunkBlocks>()

	override suspend fun generateChunk(scope: CoroutineScope) {
		val chunkCompletedSections = mutableListOf<CompletedSection>()
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

					val chunkOriginX = chunk.pos.x * 16
					val chunkOriginZ = chunk.pos.z * 16

					for (sectionPos in sectionsSet) {
						val newlyCompleted = generateSection(
							clipboard,
							offset,
							sectionPos,
							chunkOriginX,
							chunkOriginZ,
							encounter
						) ?: continue

						chunkCompletedSections.add(newlyCompleted)
					}

					returnData.complete(StoredChunkBlocks(chunkCompletedSections))
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
		encounter: Encounter?
	): CompletedSection? {
		val palette = mutableListOf<BlockData>()
		val storedBlocks = IntArray(4096)
		val sectionMinY = sectionY.shl(4)

		palette += BlockData(Blocks.AIR.defaultBlockState(), null)

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

					val index = CompletedSection.posToIndex(x, y, z)

					if (originalBlockState.isAir) {
						storedBlocks[index] = 0
						continue
					}

					val blockData = BlockData(originalBlockState, blockNBT?.nms())

					if (originalBlockState.`is`(Blocks.CHEST)) {
						checkChestFlags(encounter, blockData)
					}

					// Format the block entity
					(blockData.blockEntityTag)?.putInt("x", worldX)
					(blockData.blockEntityTag)?.putInt("y", worldY)
					(blockData.blockEntityTag)?.putInt("z", worldZ)

					val blockIndex = if (!palette.contains(blockData)) {
						palette.add(blockData)
						palette.lastIndex
					} else palette.indexOf(blockData)

					storedBlocks[index] = blockIndex
				}
			}
		}

		if (storedBlocks.all { it == 0 }) return null // don't write it if it's all empty

		return CompletedSection(
			sectionY,
			palette,
			storedBlocks
		)
	}
}

private fun checkChestFlags(encounter: Encounter?, blockData: BlockData) {
	val (_, blockNBT) = blockData
	val newPDC = CraftPersistentDataContainer(CraftPersistentDataTypeRegistry())

	if (!blockNBT!!.contains("CustomName")) return
	val name = blockNBT.getString("CustomName")

	if (!name.contains("Secondary: ", true)) {
		println(1)
		val chestType = name.substringAfter("Secondary: ").substringBefore("\"")

		SecondaryChest[chestType]?.let {
			blockData.blockEntityTag = it.NBT
			newPDC.set(SECONDARY_CHEST, STRING, it.name)
		}
	}

	if (!name.contains("Encounter Chest", true)) {
		println(2)
		encounter?.let {
			println(3)

			blockData.blockEntityTag = it.constructChestNBT()
			newPDC.set(ENCOUNTER, STRING, it.identifier)
		}
	}

	println(NbtUtils.structureToSnbt(blockData.blockEntityTag))
	blockData.blockEntityTag?.merge((newPDC).toTagCompound()) // TODO fix this

	println(NbtUtils.structureToSnbt(blockData.blockEntityTag))
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
		val identifier: String
	) {
		fun getEncounter(): Encounter = Encounters[identifier]!!
	}
}
