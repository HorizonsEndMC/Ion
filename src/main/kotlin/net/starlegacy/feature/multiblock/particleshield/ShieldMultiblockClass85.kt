package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.MultiblockShape

object ShieldMultiblockClass85 : SphereShieldMultiblock() {
	override val maxRange = 35
	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = "&8Class &b8.5"
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
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
				for (x in -4..-1) x(x).anyStairs()
				x(+0).ironBlock()
				for (x in +1..+4) x(x).anyStairs()
			}

			y(+0) {
				for (x in -4..-2) x(x).anyGlassPane()
				x(-1).anyGlass()
				x(+0).sponge()
				x(+1).anyGlass()
				for (x in +2..+4) x(x).anyGlassPane()
			}
		}

		z(+2) {
			y(-1) {
				for (x in -4..-2) x(x).ironBlock()
				x(-1).ironBlock()
				x(+1).ironBlock()
				for (x in +2..+4) x(x).ironBlock()
			}

			y(+0) {
				for (x in -4..-2) x(x).anyGlass()
				x(-1).sponge()
				x(+1).sponge()
				for (x in +2..+4) x(x).anyGlass()
			}
		}

		z(+3) {
			y(-1) {
				for (x in -4..-1) x(x).anyStairs()
				x(+0).ironBlock()
				for (x in +1..+4) x(x).anyStairs()
			}

			y(+0) {
				for (x in -4..-2) x(x).anyGlassPane()
				x(-1).anyGlass()
				x(+0).sponge()
				x(+1).anyGlass()
				for (x in +2..+4) x(x).anyGlassPane()
			}
		}
	}
}
