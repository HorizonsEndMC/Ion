package net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace

import org.bukkit.Material

object PowerFurnaceMultiblockTier2 : PowerFurnaceMultiblock("&eTier 2") {
	override val tierMaterial = Material.GOLD_BLOCK
	override val maxPower = 50_000
	override val burnTime = 300
}
