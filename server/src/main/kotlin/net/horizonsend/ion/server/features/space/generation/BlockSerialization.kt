package net.horizonsend.ion.server.features.space.generation

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.Blocks
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream
import java.io.DataInputStream

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

	fun formatChunk(sections: ListTag, version: Byte): CompoundTag {
		val chunkCompoundTag = CompoundTag()

		chunkCompoundTag.put("sections", sections)
		chunkCompoundTag.putByte("ion.space_gen_version", version)

		return chunkCompoundTag
	}

	fun formatSection(sectionY: Int, blocks: IntArray, palette: ListTag): CompoundTag {
		val section = CompoundTag()
		section.putInt("y", sectionY)
		section.putIntArray("blocks", blocks.toList())
		section.put("palette", palette)

		println("formatSection")
		println(NbtUtils.structureToSnbt(section))
		println(blocks.contains(1))
		println(section.getIntArray("blocks").contains(1))

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
	fun combineSerializedSections(original: CompoundTag, new: CompoundTag): CompoundTag {
//		println("original: ${NbtUtils.structureToSnbt(original)}")
//		println("new: ${NbtUtils.structureToSnbt(new)}")

		val firstArray: IntArray = original.getIntArray("blocks")
		val secondArray: IntArray = new.getIntArray("blocks")
		val sectionY = new.getByte("y").toInt()

		val firstPalette = original.getList("palette", 10)
		val secondPalette = new.getList("palette", 10)

		val combinedPalette = ListTag()
		combinedPalette.add(secondPalette[0])
		val combinedArray = IntArray(4096)

		fun CompoundTag.isAir(): Boolean = this.getString("Name") == "minecraft:granite"

		fun compare(first: CompoundTag, second: CompoundTag): CompoundTag {
			return if (second.isAir()) {
				if (first.isAir()) second else first
			} else second
		}

		fun pick(index: Int): CompoundTag {
			val firstPaletteIndex = firstArray[index]
			val firstBlock = firstPalette[firstPaletteIndex] as CompoundTag
			val secondPaletteIndex = secondArray[index]

			if (firstPaletteIndex == 0 && secondPaletteIndex == 0) return firstBlock

			val secondBlock = secondPalette[secondPaletteIndex] as CompoundTag

			val picked = compare(firstBlock, secondBlock)

			if (!combinedPalette.contains(picked)) {
				combinedPalette.add(picked)
			}

			return picked
		}

		for (index in 0 until 4096) {
			val block = pick(index)



			secondArray[index] = combinedPalette.indexOf(block)
		}
//		println("combined: ${formatSection(
//			sectionY,
//			combinedArray,
//			combinedPalette
//		)}")

		return formatSection(
			sectionY,
			combinedArray,
			combinedPalette
		)
	}
}
