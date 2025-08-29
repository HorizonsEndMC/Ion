package net.horizonsend.ion.server.features.custom.blocks.pipe

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.chorusPlantData
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customItemDrop
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.misc.OrientableCustomBlock
import org.bukkit.Axis
import org.bukkit.block.BlockFace

object ReinforcedFluidPipeBlock : OrientableCustomBlock(
	identifier = CustomBlockKeys.REINFORCED_FLUID_PIPE,
	axisData = mapOf(
		Axis.Y to chorusPlantData(setOf(BlockFace.UP)),
		Axis.X to chorusPlantData(setOf(BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN)),
		Axis.Z to chorusPlantData(setOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN))
	),
	drops = BlockLoot(
		requiredTool = null,
		drops = customItemDrop(CustomItemKeys.REINFORCED_FLUID_PIPE)
	),
	customBlockItem = CustomItemKeys.REINFORCED_FLUID_PIPE
)
