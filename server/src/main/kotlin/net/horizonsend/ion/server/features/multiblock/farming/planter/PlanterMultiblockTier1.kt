package net.horizonsend.ion.server.features.multiblock.farming.planter

import org.bukkit.Material

object PlanterMultiblockTier1 : PlanterMultiblock(Material.IRON_BLOCK, 1) {
	override val regionDepth: Int = 9
	override val maxPower: Int = 50_000
}
