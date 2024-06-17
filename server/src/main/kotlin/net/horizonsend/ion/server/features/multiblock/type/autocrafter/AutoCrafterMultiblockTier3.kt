package net.horizonsend.ion.server.features.multiblock.type.autocrafter

import org.bukkit.Material

object AutoCrafterMultiblockTier3 : net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblock("&bTier 3", Material.DIAMOND_BLOCK, iterations = 6) {
	override val maxPower: Int = 600_000
}
