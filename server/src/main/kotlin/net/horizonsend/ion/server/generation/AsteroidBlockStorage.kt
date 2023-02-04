package net.horizonsend.ion.server.generation

import net.horizonsend.ion.server.ServerConfiguration
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.nms

object AsteroidBlockStorage {

	/**
	 * @param blocks: Map of packed BlockPos to BlockState?, index corresponding with position, moving x, z, then y.
	 * @param version: Data version
	 * */
	fun generateChunkData(blocks: Map<Long, BlockState?>, version: Byte): CompoundTag {
		return generateChunkData(
			index(blocks),
			version
		)
	}

	/**
	 * @param blocks: Array of BlockState, index corresponding with position, moving x, z, then y.
	 * @param version: Data version
	 * */
	fun generateChunkData(blocks: Array<BlockState?>, version: Byte): CompoundTag {
		// Divide by 16, 16, 16 to get the vertical size in chunk sections
		val sections = blocks.size.shr(4).shr(4).shr(4)

		val sectionsList = mutableListOf<CompoundTag>()

		for (section in 0..sections) {
			// Start at the section index, multiply 16 and 16 for the horizontal size
			val startIndex = section.shl(4).shl(4)
			val endIndex = (section + 1).shl(4).shl(4) - 1

			val palette = mutableSetOf<BlockState>()
			val arrayBlocks = intArrayOf()

			for (block in blocks.copyOfRange(startIndex, endIndex)) {
				val index = blocks.indexOf(block)

				if (block == null) {
					arrayBlocks[index] = 0
					continue // 0 reserved for air
				}

				palette.add(block)
				// index is position, value is palette index
				arrayBlocks[index] = palette.indexOf(block)
			}

			sectionsList.add(
				formatSection(section.toByte(), arrayBlocks, palette)
			)
		}

		return formatChunk(sectionsList, version)
	}

	private fun formatChunk(sections: List<CompoundTag>, version: Byte): CompoundTag {
		val chunkCompoundTag = CompoundTag()
		val sectionsListTag = ListTag()

		for (section in sections) { sectionsListTag.add(section) }

		chunkCompoundTag.put("sections", sectionsListTag)
		chunkCompoundTag.putByte("ion.space_gen_version", version)

		return chunkCompoundTag
	}

	private fun formatSection(sectionY: Byte, blocks: IntArray, palette: Set<BlockState>): CompoundTag {
		val section = CompoundTag()
		section.putByte("y", sectionY)
		section.putIntArray("blocks", blocks)

		val paletteListTag = ListTag()

		for (blockData in palette) {
			val paletteEntryCompoundTag = CompoundTag()

			paletteEntryCompoundTag.putString("name", blockData.toString())

			paletteListTag.add(paletteEntryCompoundTag)
		}

		section.put("palette", paletteListTag)

		return section
	}

	fun index(blocks: Map<Long, BlockState?>): Array<BlockState?> {
		val newArray = arrayOf<BlockState?>()

		for ((pos, blockState) in blocks) {
			val blocKPos = BlockPos.of(pos)
			val chunkX = blocKPos.x.shr(4)
			val chunkZ = blocKPos.z.shr(4)

			val index = chunkX + chunkZ + blocKPos.y

			newArray[index] = blockState
		}

		return newArray
	}
}

data class Asteroid(
	val x: Int,
	val y: Int,
	val z: Int,
	val palette: ServerConfiguration.Palette,
	val size: Double,
	val octaves: Int
) {
	fun materialWeights(): List<BlockState> {
		val weightedList = mutableListOf<BlockState>()

		for (material in palette.materials) {
			for (occurrence in material.value downTo 0) {
				weightedList.add(palette.getMaterial(material.key).nms)
			}
		}

		return weightedList
	}
}
