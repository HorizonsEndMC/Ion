package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.sign.Side

abstract class AbstractCommandBurst(val core: MultiblockShape.RequirementBuilder.() -> Unit) : Multiblock(),
    DisplayNameMultilblock {
	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return super.matchesUndetectedSign(sign) || sign.getSide(Side.FRONT).line(0).plainText()
			.equals("[commandburst]", ignoreCase = true)
	}

	override fun MultiblockShape.buildStructure() {
		z(5) {
			y(-1) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(0) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
		}
		z(4) {
			y(-1) {
				x(2).titaniumBlock()
				x(1).sponge()
				x(-1).sponge()
				x(-2).titaniumBlock()
			}
			y(0) {
				x(2).titaniumBlock()
				x(1).sponge()
				x(0).anyGlass()
				x(-1).sponge()
				x(-2).titaniumBlock()
			}
		}
		z(3) {
			y(-1) {
				x(2).sponge()
				x(1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.RIGHT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(0).core()
				x(-1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.RIGHT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(-2).sponge()
			}
			y(0) {
				x(2).sponge()
				x(1).anyGlass()
				x(0).anyGlass()
				x(-1).anyGlass()
				x(-2).sponge()
			}
		}
		z(2) {
			y(-1) {
				x(2).titaniumBlock()
				x(1).sponge()
				x(0).anyGlass()
				x(-1).sponge()
				x(-2).titaniumBlock()
			}
			y(0) {
				x(2).titaniumBlock()
				x(1).sponge()
				x(0).anyGlass()
				x(-1).sponge()
				x(-2).titaniumBlock()
			}
		}
		z(1) {
			y(-1) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(0) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
		}
	}
}