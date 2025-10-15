package net.horizonsend.ion.server.features.multiblock.type.starship.hyperdrive

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object HyperdriveMultiblockClass1 : HyperdriveMultiblock() {
	override val maxPower = 30_000

	override val signText = createSignText(
		line1 = "&7Class &81",
		line2 = "&bHyperdrive",
		line3 = "&cClick for",
		line4 = "&cNavigation",
	)

	override val displayName: Component get() = text("Class 1 Hyperdrive")

	override val hyperdriveClass = 1

	override fun MultiblockShape.buildStructure() {
		addHoppers(this)

		z(+0) {
			y(-1) {
				x(+0).powerInput()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).sponge()
				x(+1).anyGlass()
			}
		}
	}

	override fun buildHopperOffsets() = listOf(
		Vec3i(x = -1, y = -1, z = +0), // left hopper
		Vec3i(x = +1, y = -1, z = +0) // right hopper
	)
}
