package net.horizonsend.ion.server.features.custom.blocks.misc

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import java.util.function.Supplier

abstract class DirectionalCustomBlock(
	identifier: String,
	val faceData: Map<BlockFace, BlockData>,
	drops: BlockLoot,
	customBlockItem: Supplier<CustomBlockItem>
) : CustomBlock(identifier, faceData[BlockFace.NORTH]!!, drops, customBlockItem) {
	val bukkitFaceLookup = faceData.entries.associateTo(Object2ObjectOpenHashMap()) { entry -> entry.value to entry.key }
	val nmsFaceLookup = faceData.entries.associateTo(Object2ObjectOpenHashMap()) { entry -> entry.value.nms to entry.key }

	fun getFace(data: BlockData): BlockFace {
		return bukkitFaceLookup[data] ?: throw NoSuchElementException("Face for $data does not exist")
	}
	fun getFace(data: BlockState): BlockFace {
		return nmsFaceLookup[data] ?: throw NoSuchElementException("Face for $data does not exist")
	}
}
