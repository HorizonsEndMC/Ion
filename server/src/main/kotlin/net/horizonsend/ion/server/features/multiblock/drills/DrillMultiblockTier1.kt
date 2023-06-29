package net.horizonsend.ion.server.features.multiblock.drills

import org.bukkit.Material

object DrillMultiblockTier1 : DrillMultiblock("&7Tier 1", Material.IRON_BLOCK) {
	override val maxPower = 100_000
	override val radius = 2
	override val coolDown = 20
}
