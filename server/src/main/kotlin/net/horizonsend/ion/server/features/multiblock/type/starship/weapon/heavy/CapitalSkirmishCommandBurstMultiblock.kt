package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.AbstractCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.CapitalSkirmishCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.SkirmishCommandBurstSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object CapitalSkirmishCommandBurstMultiblock : AbstractCommandBurstMultiblock(Material.MAGMA_BLOCK) {
	override val displayName: Component get() = text("Capital Skirmish Burst")
	override val description: Component get() = text("AOE Speed Pulse")

	override val name: String = "capskirmburst"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Capital Skirmish",
		"&7&cCommand Burst&7",
		"&7-=[&c==&a==&b==&7]=-"
	)

	override fun MultiblockShape.buildStructure() {
		z(4) {
			y(-1) {
				x(3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.TOP,
						shape = Stairs.Shape.INNER_LEFT
					)
				)
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.TOP,
						shape = Stairs.Shape.INNER_RIGHT
					)
				)
				x(-3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(0) {
				x(3).anyGlass()
				x(2).anyGlass()
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyGlass()
				x(-3).anyGlass()
			}
			y(1) {
				x(3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.INNER_LEFT
					)
				)
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.INNER_RIGHT
					)
				)
				x(-3).anyStairs(
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
				x(3).titaniumBlock()
				x(2).sponge()
				x(1).anyGlass()
				x(0).anyGlass()
				x(-1).anyGlass()
				x(-2).sponge()
				x(-3).titaniumBlock()
			}
			y(0) {
				x(3).titaniumBlock()
				x(2).sponge()
				x(1).anyGlass()
				x(0).anyGlass()
				x(-1).anyGlass()
				x(-2).sponge()
				x(-3).titaniumBlock()
			}
			y(1) {
				x(3).titaniumBlock()
				x(2).sponge()
				x(1).anyGlass()
				x(0).anyGlass()
				x(-1).anyGlass()
				x(-2).sponge()
				x(-3).titaniumBlock()
			}
		}
		z(2) {
			y(-1) {
				x(3).sponge()
				x(2).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.LEFT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.RIGHT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(0).anyGlass()
				x(-1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.LEFT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(-2).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.RIGHT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(-3).sponge()
			}
			y(0) {
				x(3).sponge()
				x(2).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.LEFT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.RIGHT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(0).type(core)
				x(-1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.LEFT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(-2).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.RIGHT,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(-3).sponge()
			}
			y(1) {
				x(3).sponge()
				x(2).anyGlass()
				x(1).anyGlass()
				x(0).anyGlass()
				x(-1).anyGlass()
				x(-2).anyGlass()
				x(-3).sponge()
			}
		}
		z(1) {
			y(-1) {
				x(3).titaniumBlock()
				x(2).sponge()
				x(1).anyGlass()
				x(0).anyGlass()
				x(-1).anyGlass()
				x(-2).sponge()
				x(-3).titaniumBlock()
			}
			y(0) {
				x(3).titaniumBlock()
				x(2).sponge()
				x(1).anyGlass()
				x(0).anyGlass()
				x(-1).anyGlass()
				x(-2).sponge()
				x(-3).titaniumBlock()
			}
			y(1) {
				x(3).titaniumBlock()
				x(2).sponge()
				x(1).anyGlass()
				x(0).anyGlass()
				x(-1).anyGlass()
				x(-2).sponge()
				x(-3).titaniumBlock()
			}
		}
		z(0) {
			y(-1) {
				x(3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.TOP,
						shape = Stairs.Shape.INNER_RIGHT
					)
				)
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.TOP,
						shape = Stairs.Shape.INNER_LEFT
					)
				)
				x(-3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(0) {
				x(3).anyGlass()
				x(2).anyGlass()
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyGlass()
				x(-3).anyGlass()
			}
			y(1) {
				x(3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.INNER_RIGHT
					)
				)
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.INNER_LEFT
					)
				)
				x(-3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
		}
	}

	override fun createSubsystem(starship: Starship, sign: Sign, multiblock: Multiblock): AbstractCommandBurstSubsystem<*> {
		return CapitalSkirmishCommandBurstSubsystem(starship, sign, this)
	}
}
