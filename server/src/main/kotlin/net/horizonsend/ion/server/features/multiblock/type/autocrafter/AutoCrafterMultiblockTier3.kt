package net.horizonsend.ion.server.features.multiblock.type.autocrafter

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import org.bukkit.Material

object AutoCrafterMultiblockTier3 : AutoCrafterMultiblock(text("Tier 3", AQUA), Material.DIAMOND_BLOCK, iterations = 6) {
	override val maxPower: Int = 600_000
}

object AutoCrafterMultiblockTier3Mirrored : AutoCrafterMultiblockMirrored("&bTier 3", Material.DIAMOND_BLOCK, iterations = 6) {
	override val maxPower: Int = 600_000
}
