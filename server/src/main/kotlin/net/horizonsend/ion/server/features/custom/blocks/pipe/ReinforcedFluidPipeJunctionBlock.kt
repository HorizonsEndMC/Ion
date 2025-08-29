package net.horizonsend.ion.server.features.custom.blocks.pipe

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.chorusPlantData
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customItemDrop
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import org.bukkit.block.BlockFace

object ReinforcedFluidPipeJunctionBlock : CustomBlock(
	key = CustomBlockKeys.REINFORCED_FLUID_PIPE_JUNCTION,
	blockData = chorusPlantData(setOf(BlockFace.UP, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH)),
	drops = BlockLoot(
		requiredTool = null,
		drops = customItemDrop(CustomItemKeys.REINFORCED_FLUID_PIPE_JUNCTION)
	),
	customBlockItem = CustomItemKeys.REINFORCED_FLUID_PIPE_JUNCTION
)
