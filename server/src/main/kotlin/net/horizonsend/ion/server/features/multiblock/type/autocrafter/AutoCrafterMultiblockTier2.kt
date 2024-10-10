package net.horizonsend.ion.server.features.multiblock.type.autocrafter

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Material

object AutoCrafterMultiblockTier2 : AutoCrafterMultiblock(text("Tier 2", YELLOW), Material.GOLD_BLOCK, iterations = 4) {
	override val maxPower: Int = 400_000
}

object AutoCrafterMultiblockTier2Mirrored : AutoCrafterMultiblockMirrored(text("Tier 2", YELLOW), Material.GOLD_BLOCK, iterations = 4) {
	override val maxPower: Int = 400_000
}
