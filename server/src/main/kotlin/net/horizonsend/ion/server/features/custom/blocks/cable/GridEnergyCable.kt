package net.horizonsend.ion.server.features.custom.blocks.cable

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

object GridEnergyCable : OrientableCustomBlock(
	identifier = CustomBlockKeys.GRID_ENERGY_CABLE,
	axisData = mapOf(
		Axis.Y to chorusPlantData(setOf(BlockFace.DOWN)),
		Axis.X to chorusPlantData(setOf(BlockFace.WEST, BlockFace.EAST, BlockFace.UP)),
		Axis.Z to chorusPlantData(setOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP))
	),
	drops = BlockLoot(
		requiredTool = null,
		drops = customItemDrop(CustomItemKeys.GRID_ENERGY_CABLE)
	),
	customBlockItem = CustomItemKeys.GRID_ENERGY_CABLE
), WrenchRemovable {
	override fun decorateItem(itemStack: ItemStack, block: Block) {}
}

