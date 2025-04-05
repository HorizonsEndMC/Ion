package net.horizonsend.ion.server.features.multiblock.type.farming.planter

import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import org.bukkit.Material

object PlanterMultiblockTier1 : PlanterMultiblock(Material.IRON_BLOCK, 1, DARK_GRAY) {
	override val regionDepth: Int = 9
	override val maxPower: Int = 50_000
}
