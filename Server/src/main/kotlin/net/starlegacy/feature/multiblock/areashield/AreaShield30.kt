package net.starlegacy.feature.multiblock.areashield

import net.starlegacy.feature.multiblock.LegacyMultiblockShape

object AreaShield30 : AreaShield(radius = 30) {
	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-3).anyStairs()
				x(-2).stoneBrick()
				x(-1).diamondBlock()
				x(+0).wireInputComputer()
				x(+1).diamondBlock()
				x(+2).stoneBrick()
				x(+3).anyStairs()
			}

			for (i in 0..2) {
				y(i) {
					x(-1).anyGlassPane()
					x(+0).anyGlass()
					x(+1).anyGlassPane()
				}
			}
		}

		z(+1) {
			y(-1) {
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
				x(+2).ironBlock()
				x(+3).ironBlock()
			}

			for (i in 0..2) {
				y(i) {
					x(-1).anyGlass()
					x(+0).sponge()
					x(+1).anyGlass()
				}
			}
		}

		z(+2) {
			y(-1) {
				x(-3).anyStairs()
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).diamondBlock()
				x(+1).anyStairs()
				x(+2).anyStairs()
				x(+3).anyStairs()
			}

			for (i in 0..2) {
				y(i) {
					x(-1).anyGlassPane()
					x(+0).anyGlass()
					x(+1).anyGlassPane()
				}
			}
		}
	}
}