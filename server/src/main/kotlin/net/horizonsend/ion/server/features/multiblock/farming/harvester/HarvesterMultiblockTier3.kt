package net.horizonsend.ion.server.features.multiblock.farming.harvester

import org.bukkit.Material
import net.kyori.adventure.text.format.NamedTextColor

object HarvesterMultiblockTier3 : HarvesterMultiblock(Material.EMERALD_BLOCK, 3, NamedTextColor.GREEN) {
	override val regionDepth: Int = 18
	override val maxPower: Int = 200_000
}
