package net.starlegacy.feature.multiblock.autocrafter

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object AutoCrafterMultiblockTier1 : AutoCrafterMultiblock("&8Tier 1", Material.IRON_BLOCK, iterations = 2) {
    override val advancement = SLAdvancement.AUTO_CRAFTER_ONE
    override val maxPower: Int = 200_000
}
