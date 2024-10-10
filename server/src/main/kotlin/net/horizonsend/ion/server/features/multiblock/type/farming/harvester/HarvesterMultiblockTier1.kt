package net.horizonsend.ion.server.features.multiblock.type.farming.harvester

import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import org.bukkit.Material

object HarvesterMultiblockTier1 : HarvesterMultiblock(Material.IRON_BLOCK, 1, DARK_GRAY) {
	override val regionDepth: Int = 9
	override val maxPower: Int = 50_000
}
