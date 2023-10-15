package net.horizonsend.ion.server.features.multiblock.autocrafter

import org.bukkit.Material

object AutoCrafterMultiblockTier3 : AutoCrafterMultiblock("&bTier 3", Material.DIAMOND_BLOCK, iterations = 6) {
	override val maxPower: Int = 600_000
}
