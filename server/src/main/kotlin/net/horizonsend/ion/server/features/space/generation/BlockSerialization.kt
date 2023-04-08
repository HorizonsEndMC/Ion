package net.horizonsend.ion.server.features.space.generation

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.world.level.block.Blocks
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream

object BlockSerialization {
	val AIR: CompoundTag = NbtUtils.writeBlockState(Blocks.AIR.defaultBlockState())

	fun readChunkCompoundTag(chunk: Chunk, key: NamespacedKey): CompoundTag? {
		val dataContainer = chunk.persistentDataContainer.get(
			key,
			PersistentDataType.BYTE_ARRAY
		)

		return try { // get existing asteroid data
			dataContainer?.let {
				NbtIo.readCompressed(
					ByteArrayInputStream(
						dataContainer,
						0,
						dataContainer.size
					)
				)
			}
		} catch (error: Error) {
			error.printStackTrace(); CompoundTag()
		}
	}

	fun formatChunk(sections: ListTag, version: Byte): CompoundTag {
		val chunkCompoundTag = CompoundTag()

		chunkCompoundTag.put("sections", sections)
		chunkCompoundTag.putByte("ion.space_gen_version", version)

		return chunkCompoundTag
	}

	fun formatSection(sectionY: Int, blocks: IntArray, palette: ListTag): CompoundTag {
		val section = CompoundTag()
		section.putInt("y", sectionY)
		section.putIntArray("blocks", blocks)
		section.put("palette", palette)

		return section
	}

	fun posToIndex(pos: BlockPos): Int {
		return pos.x or (pos.z shl 4) or (pos.y shl 8)
	}

	fun posToIndex(x: Int, y: Int, z: Int): Int {
		return x or (z shl 4) or (y shl 8)
	}

	/**
	 * Combines the block storage of two compound tags
	 *
	 * Where there are conflicts, the second will be preferred.
	 **/
	fun combineSectionBlockStorage(first: CompoundTag, second: CompoundTag): CompoundTag {
		val combinedPalette = mutableListOf<Tag>()
		val combinedBlocks = arrayOfNulls<Int>(4096)
		val sectionY = second.getByte("y").toInt()

		combinedPalette.add(AIR)

		val firstBlocks: IntArray = first.getIntArray("blocks")
		val firstPalette = first.getList("palette", 10)

		val secondBlocks: IntArray = second.getIntArray("blocks")
		val secondPalette = second.getList("palette", 10)

		if (secondBlocks.isEmpty() && firstBlocks.isEmpty()) return formatSection(sectionY, IntArray(4096), ListTag())
		if (firstPalette.isEmpty() && secondPalette.isEmpty()) return formatSection(sectionY, IntArray(4096), ListTag())

		val firstMap = firstBlocks.associateWith {
			if (firstPalette.isNotEmpty()) { firstPalette[firstBlocks[it]] } else AIR
		}

		val secondMap =	secondBlocks.associateWith {
			if (secondPalette.isNotEmpty()) { secondPalette[secondBlocks[it]] } else AIR
		}

		fun isAir(tag: Tag?): Boolean = (tag as? CompoundTag)?.getString("Name") == "minecraft:air"

		fun addCombined(block: Tag): Int {
			return if (!combinedPalette.contains(block)) {
				combinedPalette.add(block)
				combinedPalette.lastIndex
			} else combinedPalette.indexOf(block)
		}

		fun pick(index: Int): Int {
			if (firstMap[index] == null && secondMap[index] != null) return addCombined(secondMap[index]!!)
			if (isAir(firstMap[index]) && secondMap[index] != null) return addCombined(secondMap[index]!!)

			if (secondMap[index] == null && firstMap[index] != null) return addCombined(firstMap[index]!!)
			if (isAir(secondMap[index]) && firstMap[index] != null) return addCombined(firstMap[index]!!)

			return 0
		}

		// Iterate through both palettes to
		for (index in 0 until 4096) {
			combinedBlocks[index] = pick(index)
		}

		val finishedCombinedBlocks = combinedBlocks.requireNoNulls().toIntArray()
		val finishedPalette = ListTag()
		finishedPalette.addAll(combinedPalette)

		return formatSection(sectionY, finishedCombinedBlocks, finishedPalette)
	}
}
