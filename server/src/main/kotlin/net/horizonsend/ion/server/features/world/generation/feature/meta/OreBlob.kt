package net.horizonsend.ion.server.features.world.generation.feature.meta

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource

class OreBlob(
	val x: Int,
	val y: Int,
	val z: Int,
	val positions: Array<Vec3i>
) {
	companion object {
		fun random(randomSource: RandomSource): OreBlob {
			TODO()
		}

		fun fromCompound(tag: CompoundTag): OreBlob {
			TODO()
		}

		fun toCompound(blob: OreBlob): CompoundTag {
			TODO()
		}

		val bySize = mutableMapOf<Int, MutableSet<Array<Vec3i>>>()

		fun registerBlob(positions: Array<Vec3i>): Array<Vec3i> {
			bySize.getOrPut(positions.size) { mutableSetOf() }.add(positions)
			return positions
		}
	}
}
