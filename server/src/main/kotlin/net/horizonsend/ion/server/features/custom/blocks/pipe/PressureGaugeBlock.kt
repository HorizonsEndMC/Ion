package net.horizonsend.ion.server.features.custom.blocks.pipe

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customItemDrop
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.GaugeCustomBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.WrenchRemovable
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.CommandBlock

object PressureGaugeBlock : CustomBlock(
	key = CustomBlockKeys.PRESSURE_GAUGE,
	blockData = Material.COMMAND_BLOCK.createBlockData { t ->
		t as CommandBlock
		t.facing = BlockFace.UP
		t.isConditional = true
	},
	drops = BlockLoot(
		requiredTool = null,
		drops = customItemDrop(CustomItemKeys.PRESSURE_GAUGE)
	),
	customBlockItem = CustomItemKeys.PRESSURE_GAUGE
), WrenchRemovable, GaugeCustomBlock by GaugeCustomBlock.CommandBlockGaugeCustomBlock
