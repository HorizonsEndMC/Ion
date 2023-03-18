package net.horizonsend.ion.server.features.space.generation

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

	/**
	 * Combines the block storage of two compound tags
	 *
	 * Where there are conflicts, the second will be preferred.
	 **/
	fun combineSectionBlockStorage(first: CompoundTag, second: CompoundTag): CompoundTag {
		val combinedPalette = mutableSetOf<Tag>()
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

		// Iterate through both palettes to
		for (index in 0 until 4096) {
			val firstBlock = firstMap[index]
			val secondBlock = secondMap[index]

			val block: Int = if (secondBlocks[index] == 0) {
				if (firstBlocks[index] == 0) {
					0
				} else {
					combinedPalette.indexOf(firstBlock)
				}
			} else {
				combinedPalette.indexOf(secondBlock)
			}

			combinedBlocks[index] = block
		}

		val finishedCombinedBlocks = combinedBlocks.requireNoNulls().toIntArray()
		val finishedPalette = ListTag()

		return formatSection(sectionY, finishedCombinedBlocks, finishedPalette)
	}
}
