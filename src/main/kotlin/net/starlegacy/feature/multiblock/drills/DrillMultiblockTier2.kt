package net.starlegacy.feature.multiblock.drills

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object DrillMultiblockTier2 : DrillMultiblock("&bTier 2", Material.DIAMOND_BLOCK) {
	override val advancement = SLAdvancement.DRILL_TWO
	override val maxPower = 200_000
	override val radius = 3
	override val coolDown = 15
}
