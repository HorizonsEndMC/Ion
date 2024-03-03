package net.horizonsend.ion.server.features.multiblock.type.autocrafter

import org.bukkit.Material

object AutoCrafterMultiblockTier2 : net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblock("&eTier 2", Material.GOLD_BLOCK, iterations = 4) {
	override val maxPower: Int = 400_000
}
