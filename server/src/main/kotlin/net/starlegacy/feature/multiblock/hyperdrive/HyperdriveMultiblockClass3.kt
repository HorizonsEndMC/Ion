package net.starlegacy.feature.multiblock.hyperdrive

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

object HyperdriveMultiblockClass3 : HyperdriveMultiblock() {
	override val maxPower = 75_000

	override val signText = createSignText(
		line1 = "&7Class",
		line2 = "&23",
		line3 = "&bHyperdrive",
		line4 = null
	)

	override val hyperdriveClass = 3

	override fun LegacyMultiblockShape.buildStructure() {
		addHoppers(this)

		z(+0) {
			y(-1) {
				x(0).wireInputComputer()
			}

			for (y in 0..1) y(y) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(+0).emeraldBlock()
			}

			for (y in 0..1) y(y) {
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
			}
		}

		z(+2) {
			for (y in 0..1) y(y) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}
	}

	override fun buildHopperOffsets() = listOf(
		Vec3i(x = -1, y = -1, z = +1), // left hopper
		Vec3i(x = +1, y = -1, z = +1), // right hopper
		Vec3i(x = +0, y = -1, z = +2) // rear hopper
	)
}
