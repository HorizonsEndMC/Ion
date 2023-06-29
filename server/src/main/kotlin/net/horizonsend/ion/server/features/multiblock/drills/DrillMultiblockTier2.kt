package net.horizonsend.ion.server.features.multiblock.drills

import org.bukkit.Material

object DrillMultiblockTier2 : DrillMultiblock("&bTier 2", Material.DIAMOND_BLOCK) {
	override val maxPower = 200_000
	override val radius = 3
	override val coolDown = 15
}
