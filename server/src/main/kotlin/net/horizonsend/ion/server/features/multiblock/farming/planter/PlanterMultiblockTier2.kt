package net.horizonsend.ion.server.features.multiblock.farming.planter

import org.bukkit.Material
import net.kyori.adventure.text.format.NamedTextColor

object PlanterMultiblockTier2 : PlanterMultiblock(Material.GOLD_BLOCK, 2, NamedTextColor.GOLD) {
	override val regionDepth: Int = 15
	override val maxPower: Int = 100_000
}
