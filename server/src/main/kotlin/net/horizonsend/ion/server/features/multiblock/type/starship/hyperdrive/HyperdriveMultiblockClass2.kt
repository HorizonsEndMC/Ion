package net.horizonsend.ion.server.features.multiblock.type.starship.hyperdrive

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object HyperdriveMultiblockClass2 : HyperdriveMultiblock() {
	override val maxPower = 50_000

	override val signText = createSignText(
		line1 = "&7Class &12",
		line2 = "&bHyperdrive",
		line3 = "&cClick for",
		line4 = "&cNavigation",

	)

	override val displayName: Component get() = text("Class 2 Hyperdrive")

	override val hyperdriveClass = 2

	override fun MultiblockShape.buildStructure() {
		addHoppers(this)

		z(+0) {
			y(-1) {
				x(+0).powerInput()
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
