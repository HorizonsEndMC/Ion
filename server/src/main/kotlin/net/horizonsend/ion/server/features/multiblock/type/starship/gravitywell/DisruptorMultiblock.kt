package net.horizonsend.ion.server.features.multiblock.type.starship.gravitywell

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object DisruptorMultiblock : GravityWellMultiblock() {
	override val name = "disruptor"

	override val displayName: Component get() = text("Disruptor")
	override val description: Component get() = text("Disrupts a targeted ship's hyperdrive.")

	override val signText = createSignText(
		line1 = "&2Disruptor",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override fun MultiblockShape.buildStructure() {
		z(2) {
			y(0) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).anyGlass()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-1).ironBlock()
				x(0).anyGlass()
				x(1).ironBlock()
			}
			y(2) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyGlass()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(1) {
			y(0) {
				x(-1).ironBlock()
				x(0).sponge()
				x(1).ironBlock()
			}
			y(1) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(0).diamondBlock()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(2) {
				x(-1).ironBlock()
				x(0).anyGlass()
				x(1).ironBlock()
			}
		}
		z(0) {
			y(0) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).powerInput()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-1).ironBlock()
				x(0).anyGlass()
				x(1).ironBlock()
			}
			y(2) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyGlass()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}

}
