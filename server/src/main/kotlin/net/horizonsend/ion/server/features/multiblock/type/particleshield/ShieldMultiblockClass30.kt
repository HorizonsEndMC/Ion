package net.horizonsend.ion.server.features.multiblock.type.particleshield

import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object ShieldMultiblockClass30 : SphereShieldMultiblock() {
	override val maxRange = 22
	const val SHIELD_CLASS_TEXT = "&8Class &b3.0"
	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = SHIELD_CLASS_TEXT
	)

	override val displayName: Component get() = ofChildren(legacyAmpersand.deserialize(SHIELD_CLASS_TEXT), text(" Shield"))
	override val description: Component get() = text("Protects a starship from explosion damage within a $maxRange block radius.")

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+1).anyGlassPane()
			}
		}

		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyStairs()
				x(+1).anyGlassPane()
			}
		}
	}
}
