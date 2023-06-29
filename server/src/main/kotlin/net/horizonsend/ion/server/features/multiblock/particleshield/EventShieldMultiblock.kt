package net.horizonsend.ion.server.features.multiblock.particleshield

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import org.bukkit.Material

object EventShieldMultiblock : SphereShieldMultiblock() {
	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = "&8Class &d0.8i"
	)

	override val maxRange = 60
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
				x(+0).type(Material.LAPIS_BLOCK)
				x(+1).anyGlassPane()
			}
		}
	}
}
