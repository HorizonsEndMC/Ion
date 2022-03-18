package net.starlegacy.feature.multiblock.powerbank

import org.bukkit.Material

object PowerBankMultiblockTier2 : PowerBankMultiblock("&eTier 2") {
	override val maxPower = 350_000
	override val tierMaterial = Material.GOLD_BLOCK
}
