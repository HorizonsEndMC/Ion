package net.horizonsend.ion.server.features.multiblock.type.starship.navigationcomputer

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object NavigationComputerMultiblockBasic : NavigationComputerMultiblock() {
	override val signText = createSignText(
		line1 = "&aBasic",
		line2 = "&8Navigation",
		line3 = "&8Computer",
		line4 = null
	)

	override val displayName: Component get() = text("Basic Navigation Computer")
	override val description: Component get() = text("Allows a starship to jump to set coordinates in space. Base range $baseRange.")

	override val baseRange: Int = 15000

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).anyStairs()
				x(+0).diamondBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(+0) {
				x(-1).ironBlock()
				x(+0).diamondBlock()
				x(+1).ironBlock()
			}

			y(+1) {
				x(-1).anyGlassPane()
				x(+0).sponge()
				x(+1).anyGlassPane()
			}
		}
	}
}
