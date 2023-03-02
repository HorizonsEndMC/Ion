package net.starlegacy.feature.multiblock.powerfurnace

import net.starlegacy.util.Vec3i
import org.bukkit.Material

object PowerFurnaceMultiblockTier2 : PowerFurnaceMultiblock("&eTier 2") {
	override val tierMaterial = Material.GOLD_BLOCK
	override val maxPower = 50_000
	override val burnTime = 300
	override val inputComputerOffset = Vec3i(0, -1, 0)
}
