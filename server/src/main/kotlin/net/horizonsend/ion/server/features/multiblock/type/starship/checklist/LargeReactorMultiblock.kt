package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object LargeReactorMultiblock : AbstractReactorCore({ customBlock(CustomBlockKeys.LARGE_REACTOR_CORE.getValue()) }) {
	override val displayName: Component get() = text("Large Reactor")
	override val description: Component get() = text("Reactor core critical to tech 2 Cruiser and Battlecruiser functionality.")

	override val name: String = "largereactor"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Large",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
	override fun MultiblockShape.buildStructure() {
		z(4) {
			y(0) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-2).sponge()
				x(-1).titaniumBlock()
				x(0).seaLantern()
				x(1).titaniumBlock()
				x(2).sponge()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-3).ironBlock()
				x(-2).netheriteBlock()
				x(-1).netheriteBlock()
				x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
				x(1).netheriteBlock()
				x(2).netheriteBlock()
				x(3).ironBlock()
			}
			y(2) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).ironBlock()
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
				x(2).ironBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(3) {
			y(0) {
				x(-3).anyGlass()
				x(-2).seaLantern()
				x(-1).seaLantern()
				x(0).anyGlass()
				x(1).seaLantern()
				x(2).seaLantern()
				x(3).anyGlass()
			}
			y(1) {
				x(-3).anyGlass()
				x(-2).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.RIGHT, example = Material.END_ROD.createBlockData()))
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.LEFT, example = Material.END_ROD.createBlockData()))
				x(0).customBlock(CustomBlockKeys.LARGE_REACTOR_CORE.getValue())
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.RIGHT, example = Material.END_ROD.createBlockData()))
				x(2).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.LEFT, example = Material.END_ROD.createBlockData()))
				x(3).anyGlass()
			}
			y(2) {
				x(-3).anyGlass()
				x(-2).anyGlass()
				x(-1).anyGlass()
				x(0).anyGlass()
				x(1).anyGlass()
				x(2).anyGlass()
				x(3).anyGlass()
			}
		}
		z(2) {
			y(0) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-2).sponge()
				x(-1).titaniumBlock()
				x(0).seaLantern()
				x(1).titaniumBlock()
				x(2).sponge()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-3).ironBlock()
				x(-2).netheriteBlock()
				x(-1).netheriteBlock()
				x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
				x(1).netheriteBlock()
				x(2).netheriteBlock()
				x(3).ironBlock()
			}
			y(2) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).ironBlock()
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
				x(2).ironBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(6) {
			y(0) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
				x(0).redstoneBlock()
				x(1).ironBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-2).ironBlock()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
				x(0).anyGlass()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
				x(2).ironBlock()
			}
			y(2) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
				x(0).redstoneBlock()
				x(1).ironBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(5) {
			y(0) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).seaLantern()
				x(1).sponge()
				x(2).ironBlock()
			}
			y(1) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
				x(1).sponge()
				x(2).ironBlock()
			}
			y(2) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).titaniumBlock()
				x(1).sponge()
				x(2).ironBlock()
			}
		}
		z(1) {
			y(0) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).seaLantern()
				x(1).sponge()
				x(2).ironBlock()
			}
			y(1) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
				x(1).sponge()
				x(2).ironBlock()
			}
			y(2) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).titaniumBlock()
				x(1).sponge()
				x(2).ironBlock()
			}
		}
		z(0) {
			y(0) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
				x(0).redstoneBlock()
				x(1).ironBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-2).ironBlock()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(0).anyGlass()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(2).ironBlock()
			}
			y(2) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
				x(0).redstoneBlock()
				x(1).ironBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}


}

