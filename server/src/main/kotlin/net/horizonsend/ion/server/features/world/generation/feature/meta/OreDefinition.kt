package net.horizonsend.ion.server.features.world.generation.feature.meta

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState

class OreDefinition(
	val material: BlockState,
	val shape: Int, // Index in ore blob shape array
	val chance: Double
) {
	companion object {
		fun random(randomSource: RandomSource): OreDefinition {
			TODO()
		}

		fun fromCompound(tag: CompoundTag): OreDefinition {
			TODO()
		}

		fun toCompound(blob: OreDefinition): CompoundTag {
			TODO()
		}

		val bySize = mutableMapOf<Int, MutableSet<Array<Vec3i>>>()

		fun registerBlob(positions: Array<Vec3i>): Array<Vec3i> {
			bySize.getOrPut(positions.size) { mutableSetOf() }.add(positions)
			return positions
		}
	}
}
