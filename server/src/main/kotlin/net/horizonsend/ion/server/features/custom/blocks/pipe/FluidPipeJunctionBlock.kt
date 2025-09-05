package net.horizonsend.ion.server.features.custom.blocks.pipe

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.chorusPlantData
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customItemDrop
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.WrenchRemovable
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack

object FluidPipeJunctionBlock : CustomBlock(
	key = CustomBlockKeys.FLUID_PIPE_JUNCTION,
	blockData = chorusPlantData(setOf(BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH)),
	drops = BlockLoot(
		requiredTool = null,
		drops = customItemDrop(CustomItemKeys.FLUID_PIPE_JUNCTION)
	),
	customBlockItem = CustomItemKeys.FLUID_PIPE_JUNCTION
), WrenchRemovable {
	override fun decorateItem(itemStack: ItemStack, block: Block) {}
}

