package net.starlegacy.feature.multiblock.navigationcomputer

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.progression.advancement.SLAdvancement

object NavigationComputerMultiblockAdvanced : NavigationComputerMultiblock() {
	override val advancement = SLAdvancement.NAV_COMPUTER_ADVANCED

	override val signText = createSignText(
		line1 = "&6Advanced",
		line2 = "&8Navigation",
		line3 = "&8Computer",
		line4 = null
	)

	override val baseRange: Int = 50000

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
