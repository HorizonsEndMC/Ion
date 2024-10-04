package net.horizonsend.ion.server.features.multiblock.type.autocrafter

import org.bukkit.Material

object AutoCrafterMultiblockTier1 : AutoCrafterMultiblock("&8Tier 1", Material.IRON_BLOCK, iterations = 2) {
	override val maxPower: Int = 200_000
}

object AutoCrafterMultiblockTier1Mirrored : AutoCrafterMultiblockMirrored("&8Tier 1", Material.IRON_BLOCK, iterations = 2) {
	override val maxPower: Int = 200_000
}
