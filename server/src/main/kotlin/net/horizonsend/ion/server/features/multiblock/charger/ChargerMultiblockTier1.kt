package net.horizonsend.ion.server.features.multiblock.charger

import org.bukkit.ChatColor
import org.bukkit.Material

object ChargerMultiblockTier1 : ChargerMultiblock(tierText = ChatColor.GRAY.toString() + "Tier 1") {
	override val tierMaterial = Material.IRON_BLOCK
	override val maxPower = 100_000
	override val powerPerSecond = 1000
}
