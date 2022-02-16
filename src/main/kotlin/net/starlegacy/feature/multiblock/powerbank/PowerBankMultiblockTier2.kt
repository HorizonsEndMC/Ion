package net.starlegacy.feature.multiblock.powerbank

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object PowerBankMultiblockTier2 : PowerBankMultiblock("&eTier 2") {
	override val advancement = SLAdvancement.POWER_BANK_TWO
	override val maxPower = 350_000
	override val tierMaterial = Material.GOLD_BLOCK
}
