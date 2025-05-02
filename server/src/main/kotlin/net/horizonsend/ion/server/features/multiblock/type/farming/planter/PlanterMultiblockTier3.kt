package net.horizonsend.ion.server.features.multiblock.type.farming.planter

import net.kyori.adventure.text.format.NamedTextColor.GREEN
import org.bukkit.Material

object PlanterMultiblockTier3 : PlanterMultiblock(Material.EMERALD_BLOCK, 3, GREEN) {
	override val regionDepth: Int = 18
	override val maxPower: Int = 200_000
}
