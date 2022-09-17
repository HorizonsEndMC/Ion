package net.starlegacy.feature.multiblock.particleshield

import net.kyori.adventure.text.Component
import net.starlegacy.feature.multiblock.MultiblockShape

object ShieldMultiblockClass20 : SphereShieldMultiblock() {
	override val maxRange = 14
	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = "&8Class &b2.0"
	)

	// particle shields in 1.12 are broken and have 2.0 instead of the correct line 2, this is to automatically replace it
	override fun matchesSign(lines: Array<Component>): Boolean {
		val modified = lines.clone()
		modified[1] = signText[1]!!
		return super.matchesSign(modified)
	}

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
			}
		}
	}
}