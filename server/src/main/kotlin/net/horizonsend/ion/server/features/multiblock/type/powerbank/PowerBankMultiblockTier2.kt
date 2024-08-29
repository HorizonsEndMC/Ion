package net.horizonsend.ion.server.features.multiblock.type.powerbank

import org.bukkit.Material

data object PowerBankMultiblockTier2 : PowerBankMultiblock("&eTier 2") {
	override val tierMaterial = Material.GOLD_BLOCK

	override val maxPower: Int = 350_000
}
