package net.horizonsend.ion.server.features.multiblock.powerbank

import org.bukkit.Material

object PowerBankMultiblockTier3 : PowerBankMultiblock("&bTier 3") {
	override val maxPower = 500_000
	override val tierMaterial = Material.DIAMOND_BLOCK
}
