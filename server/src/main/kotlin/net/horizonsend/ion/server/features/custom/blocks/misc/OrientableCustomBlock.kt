package net.horizonsend.ion.server.features.custom.blocks.misc

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Axis
import org.bukkit.block.data.BlockData

abstract class OrientableCustomBlock(
	identifier: IonRegistryKey<CustomBlock, out CustomBlock>,
	val axisData: Map<Axis, BlockData>,
	drops: BlockLoot,
	customBlockItem: IonRegistryKey<CustomItem, out CustomItem>
) : CustomBlock(identifier, axisData[Axis.Y]!!, drops, customBlockItem) {
	val bukkitAxisLookup = axisData.entries.associateTo(Object2ObjectOpenHashMap()) { entry -> entry.value to entry.key }
	val nmsAxisLookup = axisData.entries.associateTo(Object2ObjectOpenHashMap()) { entry -> entry.value.nms to entry.key }

	fun getFace(data: BlockData): Axis {
		return bukkitAxisLookup[data] ?: throw NoSuchElementException("Face for $data does not exist")
	}
	fun getFace(data: BlockState): Axis {
		return nmsAxisLookup[data] ?: throw NoSuchElementException("Face for $data does not exist")
	}
}
