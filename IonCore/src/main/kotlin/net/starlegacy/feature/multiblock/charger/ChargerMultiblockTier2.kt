package net.starlegacy.feature.multiblock.charger

import org.bukkit.Material

object ChargerMultiblockTier2 : ChargerMultiblock(tierText = "&eTier 2") {
	override val tierMaterial = Material.GOLD_BLOCK
	override val maxPower = 200_000
	override val powerPerSecond = 2000
}
