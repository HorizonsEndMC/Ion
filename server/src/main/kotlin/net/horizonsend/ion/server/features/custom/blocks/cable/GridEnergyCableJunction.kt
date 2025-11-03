package net.horizonsend.ion.server.features.custom.blocks.cable

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

object GridEnergyCableJunction : CustomBlock(
	key = CustomBlockKeys.GRID_ENERGY_CABLE_JUNCTION,
	blockData = chorusPlantData(setOf(BlockFace.UP, BlockFace.DOWN)),
	drops = BlockLoot(
		requiredTool = null,
		drops = customItemDrop(CustomItemKeys.GRID_ENERGY_CABLE_JUNCTION)
	),
	customBlockItem = CustomItemKeys.GRID_ENERGY_CABLE_JUNCTION
), WrenchRemovable {
	override fun decorateItem(itemStack: ItemStack, block: Block) {}
}


