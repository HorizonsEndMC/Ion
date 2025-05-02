package net.horizonsend.ion.server.features.multiblock.type.farming.harvester

import net.kyori.adventure.text.format.NamedTextColor.GOLD
import org.bukkit.Material

object HarvesterMultiblockTier2 : HarvesterMultiblock(Material.GOLD_BLOCK, 2, GOLD) {
	override val regionDepth: Int = 15
	override val maxPower: Int = 100_000
}
