package net.starlegacy.feature.multiblock.powerfurnace

import org.bukkit.Material

object PowerFurnaceMultiblockTier3 : PowerFurnaceMultiblock("&bTier 3") {
	override val tierMaterial = Material.DIAMOND_BLOCK
	override val maxPower = 75_000
	override val burnTime = 400
}
