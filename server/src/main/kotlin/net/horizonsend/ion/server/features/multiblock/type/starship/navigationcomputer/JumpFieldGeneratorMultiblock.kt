package net.horizonsend.ion.server.features.multiblock.type.starship.navigationcomputer

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object JumpFieldGeneratorMultiblock : NavigationComputerMultiblock() {
	override val description: Component get() = text("Allows a starship to jump to a jump beacon")
	override val displayName: Component get() = text("Jump Field Generator")
	override val signText = createSignText(
		line1 = "&6Jump",
		line2 = "&8Field",
		line3 = "&8Generator",
		line4 = null
	)

	override val name = "jumpfieldgen"
	override val baseRange: Int = 6942069

	override fun MultiblockShape.buildStructure() {
		z(2) {
			y(0) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).sponge()
				x(1).sponge()
				x(2).ironBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).diamondBlock()
				x(1).sponge()
				x(2).ironBlock()
				x(3).ironBlock()
			}
			y(2) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).sponge()
				x(1).sponge()
				x(2).ironBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(1) {
			y(0) {
				x(-3).anyWall()
				x(-2).aluminumBlock()
				x(-1).sponge()
				x(0).aluminumBlock()
				x(1).sponge()
				x(2).aluminumBlock()
				x(3).anyWall()
			}
			y(1) {
				x(-3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(-2).aluminumBlock()
				x(-1).sponge()
				x(0).aluminumBlock()
				x(1).sponge()
				x(2).aluminumBlock()
				x(3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(2) {
				x(-3).anyWall()
				x(-2).aluminumBlock()
				x(-1).sponge()
				x(0).aluminumBlock()
				x(1).sponge()
				x(2).aluminumBlock()
				x(3).anyWall()
			}
		}
		z(0) {
			y(0) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.INNER_LEFT))
				x(-1).ironBlock()
				x(0).anyWall()
				x(1).ironBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.INNER_RIGHT))
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-3).ironBlock()
				x(-2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(-1).ironBlock()
				x(0).powerInput()
				x(1).ironBlock()
				x(2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(3).ironBlock()
			}
			y(2) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.INNER_LEFT))
				x(-1).ironBlock()
				x(0).anyWall()
				x(1).ironBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.INNER_RIGHT))
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}
}

