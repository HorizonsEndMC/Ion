package net.horizonsend.ion.server.features.multiblock.type.autocrafter

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import org.bukkit.Material

object AutoCrafterMultiblockTier1 : AutoCrafterMultiblock(text("Tier 1", DARK_GRAY), Material.IRON_BLOCK, iterations = 2) {
	override val maxPower: Int = 200_000
}

object AutoCrafterMultiblockTier1Mirrored : AutoCrafterMultiblockMirrored(text("Tier 1", DARK_GRAY), Material.IRON_BLOCK, iterations = 2) {
	override val maxPower: Int = 200_000
}
