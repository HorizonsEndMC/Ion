package net.horizonsend.ion.server.features.multiblock.type.gridpower.generator

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object GridGeneratorMultiblockMedium : GridPowerGeneratorMultiblock() {
	override val linkageOffset: Vec3i = Vec3i(0, 1, 4)

	override fun MultiblockShape.buildStructure() {
		z(4) {
			y(0) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).steelBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-2).steelBlock()
				x(-1).steelBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).steelBlock()
				x(2).steelBlock()
			}
			y(2) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).steelBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(-1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).steelBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).steelBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(3) {
			y(0) {
				x(-2).titaniumBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).titaniumBlock()
			}
			y(1) {
				x(-2).steelBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).steelBlock()
			}
			y(2) {
				x(-2).titaniumBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).titaniumBlock()
			}
			y(-1) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
		}
		z(2) {
			y(0) {
				x(-2).titaniumBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).titaniumBlock()
			}
			y(1) {
				x(-2).steelBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).steelBlock()
			}
			y(2) {
				x(-2).titaniumBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).titaniumBlock()
			}
			y(-1) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
		}
		z(1) {
			y(0) {
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
			y(1) {
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
			y(2) {
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
			y(-1) {
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
			y(3) {
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
		}
		z(0) {
			y(0) {
				x(-2).titaniumBlock()
				x(0).steelBlock()
				x(2).titaniumBlock()
			}
			y(1) {
				x(-2).steelBlock()
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
				x(2).steelBlock()
			}
			y(2) {
				x(-2).titaniumBlock()
				x(0).steelBlock()
				x(2).titaniumBlock()
			}
			y(-1) {
				x(-1).titaniumBlock()
				x(0).gridEnergyPort()
				x(1).titaniumBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
		}
	}
}
