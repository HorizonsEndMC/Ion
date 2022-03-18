package net.starlegacy.feature.multiblock.baseshield

import net.starlegacy.feature.multiblock.MultiblockShape
import org.bukkit.ChatColor

object MediumBaseShieldMultiblock : BaseShieldMultiblock() {
	override val maxPower = 50_000
	override val radius = 50

	override val signText = createSignText(
		"${ChatColor.YELLOW}Medium",
		"${ChatColor.DARK_GRAY}Base Shield",
		"",
		""
	)

	override fun MultiblockShape.buildStructure() {
		sideRings(-2, +2)

		z(+0) {
			y(-1) {
				x(+0).wireInputComputer()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
			}

			y(+1) {
				x(+0).anyStairs()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).hopper()
				x(+0).stoneBrick()
				x(+1).hopper()
			}

			y(+0) {
				for (x in -1..1) {
					x(x).diamondBlock()
				}
			}

			y(+1) {
				x(-1).anySlab()
				x(+0).stoneBrick()
				x(+1).anySlab()
			}
		}

		z(+2) {
			y(-1) {
				x(+0).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).stoneBrick()
				x(+1).anyGlassPane()
			}

			y(+1) {
				x(+0).anyStairs()
			}
		}
	}
}
