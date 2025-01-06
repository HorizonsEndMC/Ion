package net.horizonsend.ion.server.features.world.generation.feature

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.level.LevelHeightAccessor
import org.bukkit.World

class FeaturePlacementContext(
	val position: Vec3i,
	val world: World,
	val metaData: MutableMap<String, Tag> = mutableMapOf()
) {
	val heightAccessor = LevelHeightAccessor.create(world.minHeight, world.maxHeight - world.minHeight)

	fun toCompound(): CompoundTag {
		val tag = CompoundTag()
		tag.putInt("x", position.x)
		tag.putInt("y", position.y)
		tag.putInt("z", position.z)
		//TODO
		return tag
	}
}
