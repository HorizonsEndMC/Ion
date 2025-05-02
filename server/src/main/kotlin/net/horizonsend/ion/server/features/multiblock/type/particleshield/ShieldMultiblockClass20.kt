package net.horizonsend.ion.server.features.multiblock.type.particleshield

import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object ShieldMultiblockClass20 : SphereShieldMultiblock() {
	override val maxRange = 14
	const val SHIELD_CLASS_TEXT = "&8Class &b2.0"
	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = SHIELD_CLASS_TEXT
	)

	override val displayName: Component get() = ofChildren(legacyAmpersand.deserialize(SHIELD_CLASS_TEXT), text(" Shield"))
	override val description: Component get() = text("Protects a starship from explosion damage within a $maxRange block radius.")

	// particle shields in 1.12 are broken and have 2.0 instead of the correct line 2, this is to automatically replace it
	override fun matchesSign(lines: List<Component>): Boolean {
		val modified = lines.toMutableList()
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
