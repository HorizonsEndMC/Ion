package net.starlegacy.feature.multiblock.autocrafter

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object AutoCrafterMultiblockTier2 : AutoCrafterMultiblock("&eTier 2", Material.GOLD_BLOCK, iterations = 4) {
	override val advancement = SLAdvancement.AUTO_CRAFTER_TWO
	override val maxPower: Int = 400_000
}
