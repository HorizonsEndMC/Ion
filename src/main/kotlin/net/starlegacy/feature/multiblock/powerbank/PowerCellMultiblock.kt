package net.starlegacy.feature.multiblock.powerbank

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.feature.progression.advancement.SLAdvancement
import net.starlegacy.util.Vec3i

object PowerCellMultiblock : PowerStoringMultiblock() {
    override val advancement = SLAdvancement.POWER_CELL

    override val name = "powercell"

    override val signText = createSignText(
        line1 = "&6Power &8Cell",
        line2 = "------",
        line3 = null,
        line4 = "&cCompact Power"
    )

    override val maxPower = 50_000

    override val inputComputerOffset = Vec3i(0, 0, 0)

    override fun MultiblockShape.buildStructure() {
        z(+0) {
            y(+0) {
                x(-1).anyGlassPane()
                x(+0).wireInputComputer()
                x(+1).anyGlassPane()
            }
        }

        z(+1) {
            y(+0) {
                x(-1).anyGlassPane()
                x(+0).redstoneBlock()
                x(+1).anyGlassPane()
            }
        }
    }
}
