package net.horizonsend.ion.server.features.multiblock.farming.harvester

import org.bukkit.Material
import net.kyori.adventure.text.format.NamedTextColor

object HarvesterMultiblockTier2 : HarvesterMultiblock(Material.GOLD_BLOCK, 2, NamedTextColor.GOLD) {
	override val regionDepth: Int = 15
	override val maxPower: Int = 100_000
}
