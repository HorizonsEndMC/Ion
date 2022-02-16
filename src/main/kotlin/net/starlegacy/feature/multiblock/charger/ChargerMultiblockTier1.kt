package net.starlegacy.feature.multiblock.charger

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.ChatColor
import org.bukkit.Material

object ChargerMultiblockTier1 : ChargerMultiblock(tierText = ChatColor.GRAY.toString() + "Tier 1") {
	override val tierMaterial = Material.IRON_BLOCK
	override val maxPower = 100_000
	override val powerPerSecond = 1000

	override val advancement = SLAdvancement.CHARGER_ONE
}
