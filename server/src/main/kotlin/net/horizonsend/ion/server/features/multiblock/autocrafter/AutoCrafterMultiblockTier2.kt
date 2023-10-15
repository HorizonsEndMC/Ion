package net.horizonsend.ion.server.features.multiblock.autocrafter

import org.bukkit.Material

object AutoCrafterMultiblockTier2 : AutoCrafterMultiblock("&eTier 2", Material.GOLD_BLOCK, iterations = 4) {
	override val maxPower: Int = 400_000
}
