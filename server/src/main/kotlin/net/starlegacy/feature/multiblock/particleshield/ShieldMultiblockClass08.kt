package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.MultiblockShape

sealed class ShieldMultiblockClass08(private val sideX: Int) : SphereShieldMultiblock() {
	override val maxRange = 10
	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = "&8Class &b0.8"
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(0).ironBlock()
				x(sideX).sponge()
			}

			y(+0) {
				x(0).anyGlass()
				x(sideX).anyGlassPane()
			}
		}
	}
}

object ShieldMultiblockClass08Right : ShieldMultiblockClass08(1)

object ShieldMultiblockClass08Left : ShieldMultiblockClass08(-1)
