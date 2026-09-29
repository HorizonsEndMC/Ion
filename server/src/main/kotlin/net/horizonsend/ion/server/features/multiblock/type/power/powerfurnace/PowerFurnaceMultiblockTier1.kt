package net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace

import org.bukkit.Material

object PowerFurnaceMultiblockTier1 : PowerFurnaceMultiblock("&7Tier 1") {
	override val maxPower = 25_000
	override val burnTime = 20
	override val tierMaterial = Material.IRON_BLOCK
	override val cookSpeedMultiplier = 2.0
}
