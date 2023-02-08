package net.horizonsend.ion.server.generation

import net.horizonsend.ion.server.ServerConfiguration
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.nms

object AsteroidBlockStorage {
	fun formatChunk(sections: ListTag, version: Byte): CompoundTag {
		val chunkCompoundTag = CompoundTag()

		chunkCompoundTag.put("sections", sections)
		chunkCompoundTag.putByte("ion.space_gen_version", version)

		return chunkCompoundTag
	}

	fun formatSection(sectionY: Byte, blocks: IntArray, palette: ListTag): CompoundTag {
		val section = CompoundTag()
		section.putByte("y", sectionY)
		section.putIntArray("blocks", blocks)
		section.put("palette", palette)

		return section
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
