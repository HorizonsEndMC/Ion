package net.horizonsend.ion.server.features.multiblock.type.powerbank

import org.bukkit.Material

data object PowerBankMultiblockTier3 : PowerBankMultiblock("&bTier 3") {
	override val tierMaterial = Material.DIAMOND_BLOCK

	override val maxPower: Int = 500_000
}
