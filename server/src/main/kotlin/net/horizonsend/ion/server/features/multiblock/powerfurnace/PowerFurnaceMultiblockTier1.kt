package net.horizonsend.ion.server.features.multiblock.powerfurnace

import org.bukkit.Material

object PowerFurnaceMultiblockTier1 : PowerFurnaceMultiblock("&7Tier 1") {
	override val maxPower = 25_000
	override val burnTime = 200
	override val tierMaterial = Material.IRON_BLOCK
}
