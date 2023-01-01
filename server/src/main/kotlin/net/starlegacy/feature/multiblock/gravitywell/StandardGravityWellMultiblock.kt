package net.starlegacy.feature.multiblock.gravitywell

import net.starlegacy.feature.multiblock.LegacyMultiblockShape

object StandardGravityWellMultiblock : GravityWellMultiblock() {
	override val name = "gravitywell"

	override val signText = createSignText(
		line1 = "&2Gravity",
		line2 = "&8Generator",
		line3 = null,
		line4 = null
	)

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(-1) { x(+0).anyPipedInventory() }

			y(+0) {
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
			}

			y(+1) { x(+0).anyStairs() }
		}

		z(+1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
			}

			y(+1) {
				x(-1).anySlab()
				x(+0).ironBlock()
				x(+1).anySlab()
			}
		}

		z(+2) {
			y(-1) {
				x(+0).anyStairs()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).diamondBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(+0).anyStairs()
			}
		}
	}
}
