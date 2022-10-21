package net.starlegacy.feature.multiblock.hyperdrive

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.util.Vec3i

object HyperdriveMultiblockClass1 : HyperdriveMultiblock() {
	override val maxPower = 30_000

	override val signText = createSignText(
		line1 = "&7Class",
		line2 = "&81",
		line3 = "&bHyperdrive",
		line4 = null
	)

	override val hyperdriveClass = 1

	override fun LegacyMultiblockShape.buildStructure() {
		addHoppers(this)

		z(+0) {
			y(-1) {
				x(+0).wireInputComputer()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).sponge()
				x(+1).anyGlass()
			}
		}
	}

	override fun buildHopperOffsets() = listOf(
		Vec3i(x = -1, y = -1, z = +0),    // left hopper
		Vec3i(x = +1, y = -1, z = +0)     // right hopper
	)
}