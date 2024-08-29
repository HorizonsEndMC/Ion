package net.horizonsend.ion.server.features.multiblock.type.powerbank

import org.bukkit.Material

data object PowerBankMultiblockTier1 : PowerBankMultiblock("&7Tier 1") {
	override val tierMaterial = Material.IRON_BLOCK

	override val maxPower: Int = 100_000
}
