package net.starlegacy.feature.multiblock.charger

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object ChargerMultiblockTier2 : ChargerMultiblock(tierText = "&eTier 2") {
	override val tierMaterial = Material.GOLD_BLOCK
	override val maxPower = 200_000
	override val powerPerSecond = 2000

	override val advancement = SLAdvancement.CHARGER_TWO
}
