package net.horizonsend.ion.server.features.multiblock.type.defense.passive.areashield

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape

object AreaShield70 : AreaShield(radius = 70) {
	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-4).anyStairs()
				x(-3).solidBlock()
				x(-2).diamondBlock()
				x(-1).diamondBlock()
				x(+0).powerInput()
				x(+1).diamondBlock()
				x(+2).diamondBlock()
				x(+3).solidBlock()
        x(+4).anyStairs()
			}

			for (i in 0..3) {
				y(i) {
					x(-2).anyGlassPane()
          x(-1).solidBlock()
					x(+0).anyGlass()
          x(+1).solidBlock()
					x(+2).anyGlassPane()
				}
			}
		}

		z(+1) {
			y(-1) {
        x(-4).ironBlock()
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
				x(+2).ironBlock()
				x(+3).ironBlock()
        x(+4).ironBlock()
			}

			for (i in 0..3) {
				y(i) {
					x(-2).anyGlass()
					x(-1).diamondBlock()
					x(+0).sponge()
					x(+1).diamondBlock()
					x(+2).anyGlass()
				}
			}
		}

		z(+2) {
			y(-1) {
        x(-4).anyStairs()
				x(-3).anyStairs()
				x(-2).solidBlock()
				x(-1).diamondBlock()
				x(+0).diamondBlock()
				x(+1).diamondBlock()
				x(+2).solidBlock()
				x(+3).anyStairs()
        x(+4).anyStairs()
			}

			for (i in 0..3) {
				y(i) {
					x(-2).anyGlassPane()
          x(-1).solidBlock()
					x(+0).anyGlass()
          x(+1).solidBlock()
					x(+2).anyGlassPane()
				}
			}
		}
	}
}
