package net.starlegacy.feature.multiblock.drills

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object DrillMultiblockTier3 : DrillMultiblock("&aTier 3", Material.EMERALD_BLOCK) {
	override val advancement = SLAdvancement.DRILL_THREE
	override val maxPower = 300_000
	override val radius = 4
	override val coolDown = 10
}
