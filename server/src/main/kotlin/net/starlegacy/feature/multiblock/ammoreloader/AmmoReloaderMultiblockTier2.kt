package net.starlegacy.feature.multiblock.ammoreloader

import org.bukkit.ChatColor
import org.bukkit.Material

object AmmoReloaderMultiblockTier2 : AmmoReloaderMultiblock(tierText = ChatColor.YELLOW.toString() + "Tier 2") {
	override val tierMaterial = Material.GOLD_BLOCK
	override val maxPower = 200_000
}
