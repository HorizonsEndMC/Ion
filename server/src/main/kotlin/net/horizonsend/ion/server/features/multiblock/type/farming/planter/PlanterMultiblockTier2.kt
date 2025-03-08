package net.horizonsend.ion.server.features.multiblock.type.farming.planter

import net.kyori.adventure.text.format.NamedTextColor.GOLD
import org.bukkit.Material

object PlanterMultiblockTier2 : PlanterMultiblock(Material.GOLD_BLOCK, 2, GOLD) {
	override val regionDepth: Int = 15
	override val maxPower: Int = 100_000
}
