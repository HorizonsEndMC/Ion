package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.AbstractCommandBurstSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.sign.Side

abstract class AbstractCommandBurstMultiblock(val core: Material) : Multiblock(),
    DisplayNameMultilblock {
	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return super.matchesUndetectedSign(sign) || sign.getSide(Side.FRONT).line(0).plainText()
			.equals("[commandburst]", ignoreCase = true)
	}

	override fun MultiblockShape.buildStructure() {
		z(4) {
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
		z(3) {
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
		z(2) {
			y(-1) {
				x(2).sponge()
				x(1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.RIGHT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(0).type(core)
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
		z(1) {
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
		z(0) {
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

	abstract fun createSubsystem(starship: Starship, sign: Sign, multiblock: Multiblock): AbstractCommandBurstSubsystem<*>
}
