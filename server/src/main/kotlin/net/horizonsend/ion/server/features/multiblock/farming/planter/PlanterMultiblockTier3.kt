package net.horizonsend.ion.server.features.multiblock.farming.planter

import org.bukkit.Material

object PlanterMultiblockTier3 : PlanterMultiblock(Material.EMERALD_BLOCK, 3) {
	override val regionDepth: Int = 18
	override val maxPower: Int = 200_000
}
