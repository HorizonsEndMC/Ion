package net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace

import org.bukkit.Material

object PowerFurnaceMultiblockTier3 : PowerFurnaceMultiblock("&bTier 3") {
	override val tierMaterial = Material.DIAMOND_BLOCK
	override val maxPower = 250_000
	override val burnTime = 5
	//increasing burn time as blast furnaces are literally faster?
}
