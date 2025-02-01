package net.horizonsend.ion.server.features.multiblock.type.particleshield

import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object ShieldMultiblockClass08i : SphereShieldMultiblock() {
	const val SHIELD_CLASS_TEXT = "&8Class &d0.8i"
	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = "&8Class &d0.8i"
	)

	override val displayName: Component get() = ofChildren(legacyAmpersand.deserialize(SHIELD_CLASS_TEXT), text(" Shield"))
	override val description: Component get() = text("Protects a starship from explosion damage within a $maxRange block radius. Reduces incoming damage by 90% if shield health is above 80%. One additional shield allowed for every 7500 blocks.")

	override val maxRange = 10
	override val isReinforced: Boolean = true

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).sponge()
				x(+0).chetheriteBlock()
				x(+1).sponge()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}
	}
}
