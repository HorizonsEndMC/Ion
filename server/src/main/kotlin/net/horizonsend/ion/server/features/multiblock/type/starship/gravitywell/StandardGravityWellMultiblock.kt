package net.horizonsend.ion.server.features.multiblock.type.starship.gravitywell

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object StandardGravityWellMultiblock : GravityWellMultiblock() {
	override val name = "gravitywell"

	override val displayName: Component get() = text("Gravity Well")
	override val description: Component get() = text("Generates a spherical region around the starship that inhibits hyperspace travel.")

	override val signText = createSignText(
		line1 = "&2Gravity",
		line2 = "&8Generator",
		line3 = null,
		line4 = null
	)

	override fun MultiblockShape.buildStructure() {
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
