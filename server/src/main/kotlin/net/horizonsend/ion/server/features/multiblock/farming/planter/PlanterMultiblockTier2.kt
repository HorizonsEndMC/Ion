package net.horizonsend.ion.server.features.multiblock.farming.planter

import org.bukkit.Material

object PlanterMultiblockTier2 : PlanterMultiblock("&eTier 2", Material.GOLD_BLOCK) {
	override val regionDepth: Int = 15
	override val maxPower: Int = 100_000
}
