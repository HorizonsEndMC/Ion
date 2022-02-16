package net.starlegacy.feature.multiblock.powerfurnace

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object PowerFurnaceMultiblockTier3 : PowerFurnaceMultiblock("&bTier 3") {
	override val tierMaterial = Material.DIAMOND_BLOCK
	override val maxPower = 75_000
	override val burnTime = 400

	override val advancement = SLAdvancement.POWER_FURNACE_THREE
}
