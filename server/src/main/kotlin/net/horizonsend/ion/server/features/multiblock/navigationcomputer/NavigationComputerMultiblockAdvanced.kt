package net.horizonsend.ion.server.features.multiblock.navigationcomputer

import net.horizonsend.ion.server.features.multiblock.MultiblockShape

sealed class NavigationComputerMultiblockAdvanced : NavigationComputerMultiblock() {
	override val signText = createSignText(
		line1 = "&6Advanced",
		line2 = "&8Navigation",
		line3 = "&8Computer",
		line4 = null
	)

	override val baseRange: Int = 20000
}

object VerticalNavigationComputerMultiblockAdvanced : NavigationComputerMultiblockAdvanced() {
	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}

			y(+1) {
				x(-2).ironBlock()
				x(-1).anyGlassPane()
				x(+0).anyGlassPane()
				x(+1).anyGlassPane()
				x(+2).ironBlock()
			}
			y(+2) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}
		}

		z(+1) {
			y(+0) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
				x(+2).ironBlock()
			}

			y(+1) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(+0).diamondBlock()
				x(+1).sponge()
				x(+2).ironBlock()
			}
			y(+2) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
				x(+2).ironBlock()
			}
		}
	}
}

object HorizontalNavigationComputerMultiblockAdvanced : NavigationComputerMultiblockAdvanced() {
	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
			y(1) {
				x(-1).anyStairs()
				x(+0).anyGlassPane()
				x(+1).anyStairs()
			}
			y(2) {
				x(-1).ironBlock()
				x(+0).anyGlassPane()
				x(+1).ironBlock()
			}
			y(3) {
				x(-1).anyStairs()
				x(+0).anyGlassPane()
				x(+1).anyStairs()
			}
			y(4) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}

		z(+1) {
			y(0) {
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
			}
			y(1) {
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
			}
			y(2) {
				x(-1).sponge()
				x(+0).diamondBlock()
				x(+1).sponge()
			}
			y(3) {
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
			}
			y(4) {
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
			}
		}
	}
}
