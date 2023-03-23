package net.starlegacy.feature.multiblock.ammoreloader

import org.bukkit.ChatColor
import org.bukkit.Material

object AmmoReloaderTier1 : AmmoReloaderMultiblock(tierText = ChatColor.GRAY.toString() + "Tier 1") {
	override val tierMaterial = Material.IRON_BLOCK
	override val maxPower = 100_000
}
