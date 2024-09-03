package net.horizonsend.ion.server.features.multiblock.type.starship.navigationcomputer

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.LEFT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.RIGHT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.SELF
import org.bukkit.block.data.Bisected.Half.BOTTOM
import org.bukkit.block.data.Bisected.Half.TOP
import org.bukkit.block.data.type.Stairs.Shape.STRAIGHT

sealed class NavigationComputerMultiblockAdvanced : NavigationComputerMultiblock() {
	override val signText = createSignText(
		line1 = "&6Advanced",
		line2 = "&8Navigation",
		line3 = "&8Computer",
		line4 = null
	)

	override val baseRange: Int = 20000
}

data object VerticalNavigationComputerMultiblockAdvanced : NavigationComputerMultiblockAdvanced() {
	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-2).anyStairs(PrepackagedPreset.stairs(SELF, TOP, shape = STRAIGHT))
				x(-1).anyStairs(PrepackagedPreset.stairs(SELF, TOP, shape = STRAIGHT))
				x(+0).ironBlock()
				x(+1).anyStairs(PrepackagedPreset.stairs(SELF, TOP, shape = STRAIGHT))
				x(+2).anyStairs(PrepackagedPreset.stairs(SELF, TOP, shape = STRAIGHT))
			}

			y(+1) {
				x(-2).ironBlock()
				x(-1).anyGlassPane(PrepackagedPreset.pane(LEFT, RIGHT))
				x(+0).anyGlassPane(PrepackagedPreset.pane(LEFT, RIGHT))
				x(+1).anyGlassPane(PrepackagedPreset.pane(LEFT, RIGHT))
				x(+2).ironBlock()
			}
			y(+2) {
				x(-2).anyStairs(PrepackagedPreset.stairs(SELF, BOTTOM, shape = STRAIGHT))
				x(-1).anyStairs(PrepackagedPreset.stairs(SELF, BOTTOM, shape = STRAIGHT))
				x(+0).ironBlock()
				x(+1).anyStairs(PrepackagedPreset.stairs(SELF, BOTTOM, shape = STRAIGHT))
				x(+2).anyStairs(PrepackagedPreset.stairs(SELF, BOTTOM, shape = STRAIGHT))
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

data object HorizontalNavigationComputerMultiblockAdvanced : NavigationComputerMultiblockAdvanced() {
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
