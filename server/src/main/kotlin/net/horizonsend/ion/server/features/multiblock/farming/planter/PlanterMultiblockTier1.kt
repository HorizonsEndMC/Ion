package net.horizonsend.ion.server.features.multiblock.farming.planter

import org.bukkit.Material
import net.kyori.adventure.text.format.NamedTextColor

object PlanterMultiblockTier1 : PlanterMultiblock(Material.IRON_BLOCK, 1, NamedTextColor.GRAY) {
	override val regionDepth: Int = 9
	override val maxPower: Int = 50_000
}
