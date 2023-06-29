package net.horizonsend.ion.server.features.multiblock.particleshield

import net.horizonsend.ion.server.features.multiblock.MultiblockShape

object ShieldMultiblockClass65 : SphereShieldMultiblock() {
	override val maxRange = 30
	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = "&8Class &b6.5"
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}

			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
				x(+2).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
				x(+2).ironBlock()
			}

			y(+0) {
				x(-2).anyGlass()
				x(-1).anyGlass()
				x(+0).sponge()
				x(+1).anyGlass()
				x(+2).anyGlass()
			}
		}

		z(+2) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}

			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyGlassPane()
				x(+0).anyGlassPane()
				x(+1).anyGlassPane()
				x(+2).anyGlassPane()
			}
		}
	}
}
