package net.horizonsend.ion.server.features.multiblock.type.gridpower

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object SmallGridGeneratorMultiblock : GridPowerGeneratorMultiblock() {
	override fun MultiblockShape.buildStructure() {
		z(5) {
			y(-1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).steelBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-1).steelBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).steelBlock()
			}
			y(1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).steelBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(4) {
			y(-1) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
			y(0) {
				x(-1).steelBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).steelBlock()
			}
			y(1) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
		}
		z(3) {
			y(-1) {
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
			y(0) {
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
			y(1) {
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
		}
		z(2) {
			y(-1) {
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
			y(0) {
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
			y(1) {
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
			}
		}
		z(1) {
			y(-1) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
			y(0) {
				x(-1).steelBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).steelBlock()
			}
			y(1) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
		}
		z(0) {
			y(-1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).gridEnergyPort()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
			}
			y(1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).steelBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}
}
