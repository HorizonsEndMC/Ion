package net.starlegacy.feature.multiblock.generator

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object GeneratorMultiblockTier1 : GeneratorMultiblock("&8Tier 1", Material.IRON_BLOCK) {
	override val advancement = SLAdvancement.POWER_GENERATOR_ONE
	override val maxPower = 100_000
	override val speed = 1.0
}
