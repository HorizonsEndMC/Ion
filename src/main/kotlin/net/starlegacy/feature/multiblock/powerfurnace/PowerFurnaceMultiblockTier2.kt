package net.starlegacy.feature.multiblock.powerfurnace

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object PowerFurnaceMultiblockTier2 : PowerFurnaceMultiblock("&eTier 2") {
    override val tierMaterial = Material.GOLD_BLOCK
    override val maxPower = 50_000
    override val burnTime = 300

    override val advancement = SLAdvancement.POWER_FURNACE_TWO
}
