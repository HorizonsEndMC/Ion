package net.horizonsend.ion.server.features.space.generation.generators

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import net.horizonsend.ion.server.features.space.data.BlockData
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.space.encounters.Encounter
import net.horizonsend.ion.server.features.space.encounters.SecondaryChest
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.ENCOUNTER
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.SECONDARY_CHEST
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.SECONDARY_CHEST_MONEY
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.horizonsend.ion.server.miscellaneous.utils.toBukkitBlockData
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

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
					val blockNBT = if (originalBlockState.hasBlockEntity()) baseBlock.nbt else null

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

	if (!blockNBT!!.contains("CustomName")) return
	val name = blockNBT.getString("CustomName")

	if (name.contains("Secondary Chest: ", true)) {
		val chestType = name.substringAfter("Secondary Chest: ").substringBefore("\"")

		SecondaryChest[chestType]?.let {
			val money = it.money(SecondaryChest.random.nextDouble())
				.toBigDecimal().setScale(2, java.math.RoundingMode.HALF_EVEN)
				.toDouble()

			blockData.blockEntityTag = it.NBT
				.manualPDC(
					SECONDARY_CHEST.key to StringTag.valueOf(it.name),
					SECONDARY_CHEST_MONEY.key to DoubleTag.valueOf(money)
				)
		}
	}

	if (name.contains("Encounter Chest", true)) {
		encounter?.let {
			blockData.blockEntityTag = it.constructChestNBT()
				.manualPDC(ENCOUNTER.key to StringTag.valueOf(it.identifier))
		}
	}
}

private fun CompoundTag.manualPDC(vararg keys: Pair<String, Tag>): CompoundTag {
	val compound = CompoundTag()

	for ((key, tag) in keys) {
		val name = "ion:$key"
		compound.put(name, tag)
	}

	this.put(
		"PublicBukkitValues",
		compound
	)

	return this
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
