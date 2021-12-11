package net.starlegacy.feature.multiblock.powerbank

import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object PowerBankMultiblockTier3 : PowerBankMultiblock("&bTier 3") {
    override val advancement = SLAdvancement.POWER_BANK_THREE
    override val maxPower = 500_000
    override val tierMaterial = Material.DIAMOND_BLOCK
}
