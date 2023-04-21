package net.horizonsend.ion.server.features.space.generation

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntArrayTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.Blocks
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

object BlockSerialization {
	val AIR: CompoundTag = NbtUtils.writeBlockState(Blocks.AIR.defaultBlockState())

	fun readChunkCompoundTag(chunk: Chunk, key: NamespacedKey): CompoundTag? {
		val dataContainer = chunk.persistentDataContainer.get(
			key,
			PersistentDataType.BYTE_ARRAY
		)

		return try { // get existing asteroid data
			dataContainer?.let {
				val bos = ByteArrayInputStream(
					dataContainer,
					0,
					dataContainer.size
				)

				return NbtIo.read(
					DataInputStream(bos)
				)
			}
		} catch (error: Error) {
			error.printStackTrace(); CompoundTag()
		}
	}

	fun setChunkCompoundTag(chunk: Chunk, key: NamespacedKey, data: CompoundTag) {
		val bos = ByteArrayOutputStream()
		val dataStream = DataOutputStream(bos)

		NbtIo.write(data, dataStream)

		chunk.persistentDataContainer.set(
			key,
			PersistentDataType.BYTE_ARRAY,
			bos.toByteArray()
		)
	}

	fun formatChunk(sections: ListTag, version: Byte): CompoundTag {
		val chunkCompoundTag = CompoundTag()

		chunkCompoundTag.put("sections", sections)
		chunkCompoundTag.putByte("ion.space_gen_version", version)

		return chunkCompoundTag
	}

	fun formatSection(sectionY: Int, blocks: IntArray, palette: ListTag): CompoundTag {
		val section = CompoundTag()

		val x = IntArrayTag(blocks)

		val blockStates = CompoundTag()
		blockStates.put("palette", palette)
		blockStates.put("data", x)

		section.put("block_states", blockStates)
		section.putInt("y", sectionY)

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
	fun combineSerializedSections(sectionY: Int, original: CompoundTag, new: CompoundTag): CompoundTag {
		val firstStates = original.getCompound("block_states")
		val secondStates = new.getCompound("block_states")

		val firstArray: IntArray = firstStates.getIntArray("data")
		val secondArray: IntArray = secondStates.getIntArray("data")


		val firstPalette = firstStates.getList("palette", 10)
		val secondPalette = secondStates.getList("palette", 10)

		if (firstArray.isEmpty() || firstPalette.isEmpty()) return new
		if (secondArray.isEmpty() || secondPalette.isEmpty()) return original

		val combinedPalette = ListTag()
		combinedPalette.add(secondPalette[0])
		val combinedArray = IntArray(4096)

		fun CompoundTag.isAir(): Boolean = this.getString("Name") == "minecraft:air"

		fun compare(first: CompoundTag, second: CompoundTag): CompoundTag {
			return if (second.isAir()) {
				if (first.isAir()) second else first
			} else second
		}

		fun pick(index: Int): CompoundTag {
			val firstPaletteIndex = firstArray[index]
			val secondPaletteIndex = secondArray[index]

			val secondBlock = secondPalette[secondPaletteIndex] as CompoundTag
			val firstBlock = firstPalette[firstPaletteIndex] as CompoundTag

			if (firstPaletteIndex == 0 && secondPaletteIndex == 0) return firstBlock
			if (firstPaletteIndex == 0) return secondBlock
			if (secondPaletteIndex == 0) return firstBlock

			return compare(firstBlock, secondBlock)
		}

		for (index in 0 until 4096) {
			val block = pick(index)

			val blockIndex = if (!block.isAir()) {
				if (!combinedPalette.contains(block)) {
					combinedPalette.add(block)
					combinedPalette.lastIndex
				} else combinedPalette.indexOf(block)
			} else 0

			combinedArray[index] = blockIndex
		}

		return formatSection(
			sectionY,
			combinedArray,
			combinedPalette
		)
	}
}
