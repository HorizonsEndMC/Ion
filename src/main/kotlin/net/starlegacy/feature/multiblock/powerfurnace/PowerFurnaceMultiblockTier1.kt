package net.starlegacy.feature.multiblock.powerfurnace

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object PowerFurnaceMultiblockTier1 : PowerFurnaceMultiblock("&7Tier 1") {
    override val maxPower = 25_000
    override val burnTime = 200
    override val tierMaterial = Material.IRON_BLOCK

    override val advancement = SLAdvancement.POWER_FURNACE_ONE
}
