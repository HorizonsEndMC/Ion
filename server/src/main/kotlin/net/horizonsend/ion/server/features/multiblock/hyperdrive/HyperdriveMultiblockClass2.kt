package net.horizonsend.ion.server.features.multiblock.hyperdrive

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.starlegacy.util.Vec3i

object HyperdriveMultiblockClass2 : HyperdriveMultiblock() {
	override val maxPower = 50_000

	override val signText = createSignText(
		line1 = "&7Class",
		line2 = "&12",
		line3 = "&bHyperdrive",
		line4 = null
	)

	override val hyperdriveClass = 2

	override fun MultiblockShape.buildStructure() {
		addHoppers(this)

		z(+0) {
			y(-1) {
				x(+0).wireInputComputer()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(+0).diamondBlock()
			}

			y(+0) {
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
			}
		}
	}

	override fun buildHopperOffsets() = listOf(
		Vec3i(x = -1, y = -1, z = +1), // left hopper
		Vec3i(x = +1, y = -1, z = +1) // right hopper
	)
}
