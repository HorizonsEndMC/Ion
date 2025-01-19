package net.horizonsend.ion.server.features.multiblock.type.areashield

import net.horizonsend.ion.server.features.multiblock.MultiblockShape

object AreaShield5 : net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield(radius = 5) {
	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).wireInputComputer()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(+0).solidBlock()
			}

			y(+0) {
				x(+0).anyGlassPane()
			}
		}
	}
}
