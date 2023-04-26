package net.horizonsend.ion.server.features.space.generation.generators

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import net.horizonsend.ion.server.features.space.data.BlockData
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.space.encounters.Encounter
import net.horizonsend.ion.server.features.space.encounters.SecondaryChest
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.ENCOUNTER
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.SECONDARY_CHEST
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.nms
import net.starlegacy.util.toBukkitBlockData
import org.bukkit.craftbukkit.v1_19_R2.persistence.CraftPersistentDataContainer
import org.bukkit.craftbukkit.v1_19_R2.persistence.CraftPersistentDataTypeRegistry
import org.bukkit.persistence.PersistentDataType.STRING

object GenerateWreck {
	fun generateWreckSection(
		section: CompletedSection,
		clipboard: Clipboard,
		offset: BlockVector3,
		sectionY: Int,
		chunkMinX: Int,
		chunkMinZ: Int,
		encounter: Encounter?
	) {
		val sectionMinY = sectionY.shl(4)

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

					if (originalBlockState.isAir) continue

					val blockData = BlockData(originalBlockState, blockNBT?.nms())

					if (originalBlockState.`is`(Blocks.CHEST)) {
						checkChestFlags(encounter, blockData)
					}

					// Format the block entity
					(blockData.blockEntityTag)?.putInt("x", worldX)
					(blockData.blockEntityTag)?.putInt("y", worldY)
					(blockData.blockEntityTag)?.putInt("z", worldZ)

					section.setBlock(x, y, z, blockData)
				}
			}
		}

		if (section.blocks.all { it == 0 }) return // don't write it if it's all empty
	}

	fun Region.getCoveredSections(minHeight: Int, maxHeight: Int): IntRange = IntRange(
		(this.boundingBox.minimumY - minHeight).shr(4).coerceIn(minHeight.shr(4), maxHeight.shr(4) - 1),
		(this.boundingBox.maximumY - minHeight).shr(4).coerceIn(minHeight.shr(4), maxHeight.shr(4) - 1)
	)
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

	blockData.blockEntityTag?.merge((newPDC).toTagCompound()) // TODO fix this
}

/**
 * This class contains information passed to the generation function.
 * @param [x, y ,z] Origin of the asteroid.
 * @param wreckName Name of the wreck schematic
 * @param encounter Wreck encounter
 **/
data class WreckGenerationData(
	override val x: Int,
	override val y: Int,
	override val z: Int,
	val wreckName: String,
	val encounter: Encounter? = null
) : SpaceGenerationData() {
	data class WreckGen(
		val clipboard: Clipboard,
		val region: Region,
		val offset: BlockVector3,
		val encounter: Encounter?
	)

	fun worldGenData(generator: SpaceGenerator): WreckGen {
		val clipboard: Clipboard = generator.schematicMap[wreckName]!!

		val region = clipboard.region.clone()
		val targetBlockVector: BlockVector3 = BlockVector3.at(x, y, z)
		val offset: BlockVector3 = targetBlockVector.subtract(clipboard.origin)
		region.shift(offset)

		return WreckGen(
			clipboard,
			region,
			offset,
			encounter
		)
	}
}
