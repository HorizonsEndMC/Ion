package net.starlegacy.feature.multiblock.drills

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object DrillMultiblockTier1 : DrillMultiblock("&7Tier 1", Material.IRON_BLOCK) {
	override val advancement = SLAdvancement.DRILL_ONE
	override val maxPower = 100_000
	override val radius = 2
	override val coolDown = 20
}
