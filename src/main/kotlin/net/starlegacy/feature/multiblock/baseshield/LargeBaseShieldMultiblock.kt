package net.starlegacy.feature.multiblock.baseshield

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.ChatColor

object LargeBaseShieldMultiblock : BaseShieldMultiblock() {
    override val advancement = SLAdvancement.BASE_SHIELD_LARGE

    override val maxPower: Int = 50_000
    override val radius: Int = 100

    override val signText = createSignText(
        line1 = "${ChatColor.GOLD}Large",
        line2 = "${ChatColor.DARK_GRAY}Base Shield",
        line3 = null,
        line4 = null
    )

    override fun MultiblockShape.buildStructure() {
        sideRings(-3, -1, 1, 3)

        at(x = +0, y = -1, z = 0).wireInputComputer()
        at(x = +0, y = +0, z = 0).machineFurnace()

        at(x = -2, y = +0, z = 0).anyGlassPane()
        at(x = +2, y = +0, z = 0).anyGlassPane()

        for (x in arrayOf(-2, 0, 2)) {
            at(x = x, y = 0, z = 1).emeraldBlock()
            at(x = x, y = 1, z = 1).anySlab()
            at(x = x, y = 0, z = 2).anyGlassPane()
        }

        at(x = -2, y = -1, z = 1).hopper()
        at(x = +0, y = -1, z = 1).ironBlock()
        at(x = +2, y = -1, z = 1).hopper()
    }
}
