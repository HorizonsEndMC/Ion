package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.checklist

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object SmallReactorMultiblock : AbstractReactorCore({ customBlock(CustomBlockKeys.SMALL_REACTOR_CORE.getValue()) }) {
	override val displayName: Component get() = text("Small Reactor")
	override val description: Component get() = text("Reactor core critical to a tech 2 Corvette's functionality.")

	override val name: String = "smallreactor"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Small",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
	override fun MultiblockShape.buildStructure() {
		z(2) {
			y(0) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
				x(0).redstoneBlock()
				x(1).ironBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-2).ironBlock()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT))
				x(0).anyGlass()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT))
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
		z(1) {
			y(0) {
				x(-2).titaniumBlock()
				x(-1).seaLantern()
				x(0).sponge()
				x(1).seaLantern()
				x(2).titaniumBlock()
			}
			y(1) {
				x(-2).sponge()
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.LEFT, example = Material.END_ROD.createBlockData()))
				x(0).customBlock(CustomBlockKeys.SMALL_REACTOR_CORE.getValue())
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.RIGHT, example = Material.END_ROD.createBlockData()))
				x(2).sponge()
			}
			y(2) {
				x(-2).titaniumBlock()
				x(-1).seaLantern()
				x(0).sponge()
				x(1).seaLantern()
				x(2).titaniumBlock()
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
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT))
				x(0).anyGlass()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT))
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

