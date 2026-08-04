package net.horizonsend.ion.server.features.custom.blocks.pipe

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.chorusPlantData
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customItemDrop
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.misc.OrientableCustomBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.WrenchRemovable
import org.bukkit.Axis
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack

object FluidPipeBlock : OrientableCustomBlock(
	identifier = CustomBlockKeys.FLUID_PIPE,
	axisData = mapOf(
		Axis.Y to chorusPlantData(setOf(BlockFace.UP, BlockFace.DOWN)),
		Axis.X to chorusPlantData(setOf(BlockFace.WEST, BlockFace.EAST)),
		Axis.Z to chorusPlantData(setOf(BlockFace.NORTH, BlockFace.SOUTH))
	),
	drops = BlockLoot(
		requiredTool = null,
		drops = customItemDrop(CustomItemKeys.FLUID_PIPE)
	),
	customBlockItem = CustomItemKeys.FLUID_PIPE
), WrenchRemovable {
	override fun decorateItem(itemStack: ItemStack, block: Block) {}
}

