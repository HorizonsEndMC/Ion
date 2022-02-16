package net.starlegacy.feature.multiblock.baseshield

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.progression.advancement.SLAdvancement

object SmallBaseShieldMultiblock : BaseShieldMultiblock() {
	override val advancement = SLAdvancement.BASE_SHIELD_SMALL

	override val maxPower = 50_000
	override val radius = 25

	override val signText = createSignText(
		line1 = "&bBasic",
		line2 = "&8Base Shield",
		line3 = null,
		line4 = null
	)

	override fun MultiblockShape.buildStructure() {
		sideRings(-1, 1)

		z(+0) {
			y(-1) {
				x(+0).wireInputComputer()
			}

			y(+0) {
				x(+0).machineFurnace()
			}
		}

		z(+1) {
			y(-1) {
				x(+0).stoneBrick()
			}

			y(+0) {
				x(+0).goldBlock()
			}

			y(+1) {
				x(+0).anySlab()
			}
		}

		z(+2) {
			y(-1) {
				x(+0).ironBlock()
			}

			y(+0) {
				x(+0).anyGlassPane()
			}
		}
	}
}
