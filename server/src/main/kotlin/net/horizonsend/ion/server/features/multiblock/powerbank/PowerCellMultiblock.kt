package net.horizonsend.ion.server.features.multiblock.powerbank

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock

object PowerCellMultiblock : Multiblock(), PowerStoringMultiblock {
	override val name = "powercell"

	override val signText = createSignText(
		line1 = "&6Power &8Cell",
		line2 = "------",
		line3 = null,
		line4 = "&cCompact Power"
	)

	override val maxPower = 50_000

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
