package net.starlegacy.feature.multiblock.misc

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock

object TractorBeamMultiblock : Multiblock() {
	override val name = "tractorbeam"

	override val signText = createSignText(
		line1 = "&7Tractor",
		line2 = "&7Beam",
		line3 = "[-?::]",
		line4 = "[:->:]"
	)

	override fun LegacyMultiblockShape.buildStructure() {
		at(+0, +0, +0).anySlab()
		at(-1, +0, +1).anySlab()
		at(+1, +0, +1).anySlab()
		at(+0, +0, +2).anySlab()

		at(+0, +0, +1).anyGlass()
	}
}
