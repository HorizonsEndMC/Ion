package net.horizonsend.ion.server.features.multiblock.powerbank

import org.bukkit.Material

object PowerBankMultiblockTier1 : PowerBankMultiblock("&7Tier 1") {
	override val maxPower = 300_000
	override val tierMaterial = Material.IRON_BLOCK
}
