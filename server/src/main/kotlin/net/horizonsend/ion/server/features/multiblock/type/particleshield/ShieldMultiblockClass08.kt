package net.horizonsend.ion.server.features.multiblock.type.particleshield

import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

sealed class ShieldMultiblockClass08(private val sideX: Int) : SphereShieldMultiblock() {
	override val maxRange = 10
	val shieldClassText = "&8Class &b0.8"
	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = shieldClassText
	)

	override val displayName: Component get() = ofChildren(legacyAmpersand.deserialize(shieldClassText), text(" Shield (${if (sideX == 1) "Right" else "Left"})"))
	override val description: Component get() = text("Protects a starship from explosion damage within a $maxRange block radius.")

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
