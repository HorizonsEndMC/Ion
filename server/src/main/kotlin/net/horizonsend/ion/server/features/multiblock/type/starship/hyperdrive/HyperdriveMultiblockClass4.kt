package net.horizonsend.ion.server.features.multiblock.type.starship.hyperdrive

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object HyperdriveMultiblockClass4 : HyperdriveMultiblock() {
	override val signText = createSignText(
		line1 = "&7Class &64",
		line2 = "&bHyperdrive",
		line3 = "&cClick for",
		line4 = "&cNavigation",
	)

	override val maxPower = 100_000

	override val displayName: Component get() = text("Class 4 Hyperdrive")

	override val hyperdriveClass = 4

	override fun MultiblockShape.buildStructure() {
		addHoppers(this)

		z(+0) {
			y(-1) {
				x(0).powerInput()
			}

			for (y in 0..1) y(y) {
				x(-2).anyGlassPane()
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
				x(+2).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).anyGlass()
				x(+0).emeraldBlock()
				x(+1).anyGlass()
			}

			for (y in 0..1) y(y) {
				x(-2).anyGlass()
				x(-1).sponge()
				x(+0).emeraldBlock()
				x(+1).sponge()
				x(+2).anyGlass()
			}
		}

		z(+2) {
			y(-1) {
				x(0).ironBlock()
			}

			for (y in 0..1) y(y) {
				x(-2).anyGlassPane()
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
				x(+2).anyGlassPane()
			}
		}
	}

	override fun buildHopperOffsets() = listOf(
		Vec3i(x = -1, y = -1, z = +0), // front left hopper
		Vec3i(x = +1, y = -1, z = +0), // front right hopper
		Vec3i(x = -2, y = -1, z = +1), // middle left hopper
		Vec3i(x = +2, y = -1, z = +1), // middle right hopper
		Vec3i(x = -1, y = -1, z = +2), // rear left hopper
		Vec3i(x = +1, y = -1, z = +2) // rear left hopper
	)
}
