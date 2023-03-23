package net.starlegacy.feature.multiblock.ammoreloader

import org.bukkit.ChatColor
import org.bukkit.Material

object AmmoReloaderMultiblockTier3 : AmmoReloaderMultiblock(tierText = ChatColor.AQUA.toString() + "Tier 3") {
	override val tierMaterial = Material.DIAMOND_BLOCK
	override val maxPower = 300_000
}
