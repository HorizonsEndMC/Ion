package net.starlegacy.feature.multiblock.autocrafter

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object AutoCrafterMultiblockTier3 : AutoCrafterMultiblock("&bTier 3", Material.DIAMOND_BLOCK, iterations = 6) {
	override val advancement = SLAdvancement.AUTO_CRAFTER_THREE
	override val maxPower: Int = 600_000
}
