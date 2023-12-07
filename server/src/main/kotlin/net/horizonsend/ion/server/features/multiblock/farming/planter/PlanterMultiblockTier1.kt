package net.horizonsend.ion.server.features.multiblock.farming.planter

import org.bukkit.Material

object PlanterMultiblockTier1 : PlanterMultiblock("&8Tier 1", Material.IRON_BLOCK) {
	override val regionDepth: Int = 9
	override val maxPower: Int = 50_000
}
