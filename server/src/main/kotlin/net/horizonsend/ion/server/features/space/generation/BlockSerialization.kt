package net.horizonsend.ion.server.features.space.generation

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream

object BlockSerialization {
	fun readChunkCompoundTag(chunk: Chunk, key: NamespacedKey): CompoundTag {
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
			} ?: CompoundTag()
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
	fun combineSectionBlockStorage(first: CompoundTag, second: CompoundTag, holderLookup: HolderLookup<Block>): CompoundTag {
		val combinedPalette = mutableSetOf<BlockState>()
		val combinedBlocks = arrayOfNulls<Int>(4096)
		val sectionY = second.getByte("y").toInt()

		combinedPalette.add(Blocks.AIR.defaultBlockState())

		val firstBlocks: IntArray = first.getIntArray("blocks")
		val firstPalette = first.getList("palette", 10)
		val firstMap = firstBlocks.associateWith {
			val b = NbtUtils.readBlockState(holderLookup, firstPalette[firstBlocks[it]] as CompoundTag)
			combinedPalette.add(b)

			return@associateWith b
		}

		val secondBlocks: IntArray = second.getIntArray("blocks")
		val secondPalette = second.getList("palette", 10)
		val secondMap = secondBlocks.associateWith {
			val b = NbtUtils.readBlockState(holderLookup, secondPalette[firstBlocks[it]] as CompoundTag)
			combinedPalette.add(b)

			return@associateWith b
		}

		// Iterate through both palettes to
		for (index in 0 until 4096) {
			val firstBlock = firstMap[index]!! // Not null
			val secondBlock = secondMap[index]!!

			val block: Int = if (secondBlock.isAir) {
				if (firstBlock.isAir) {
					0
				} else { combinedPalette.indexOf(firstBlock) }
			} else { combinedPalette.indexOf(secondBlock) }

			combinedBlocks[index] = block
		}

		val finishedCombinedBlocks = combinedBlocks.requireNoNulls().toIntArray()
		val finishedPalette = ListTag()

		combinedPalette.forEach { blockState -> finishedPalette.add(NbtUtils.writeBlockState(blockState)) }

		return formatSection(sectionY, finishedCombinedBlocks, finishedPalette)
	}
}
