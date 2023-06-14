package net.starlegacy.feature.multiblock.navigationcomputer

import net.starlegacy.feature.multiblock.LegacyMultiblockShape

object NavigationComputerMultiblockBasic : NavigationComputerMultiblock() {
	override val signText = createSignText(
		line1 = "&aBasic",
		line2 = "&8Navigation",
		line3 = "&8Computer",
		line4 = null
	)

	override val baseRange: Int = 15000

	override fun LegacyMultiblockShape.buildStructure() {
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
