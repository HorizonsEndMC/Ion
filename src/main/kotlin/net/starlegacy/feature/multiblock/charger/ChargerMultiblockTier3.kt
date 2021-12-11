package net.starlegacy.feature.multiblock.charger

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.ChatColor
import org.bukkit.Material

object ChargerMultiblockTier3 : ChargerMultiblock(ChatColor.AQUA.toString() + "Tier 3") {
    override val tierMaterial = Material.DIAMOND_BLOCK
    override val maxPower = 300_000
    override val powerPerSecond = 3000

    override val advancement = SLAdvancement.CHARGER_THREE
}
