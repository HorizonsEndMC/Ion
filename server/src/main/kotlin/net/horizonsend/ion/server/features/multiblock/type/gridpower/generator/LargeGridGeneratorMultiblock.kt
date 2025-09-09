package net.horizonsend.ion.server.features.multiblock.type.gridpower.generator

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object LargeGridGeneratorMultiblock : GridPowerGeneratorMultiblock() {
	override val linkageOffset: Vec3i = Vec3i(0, 2, 10)

	override fun MultiblockShape.buildStructure() {
		z(7) {
			y(0) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-2).steelBlock()
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
				x(2).steelBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(2) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(-1).netheriteBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).netheriteBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(3) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(4) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).steelBlock()
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
				x(2).steelBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(-1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(5) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(3) {
			y(0) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-2).steelBlock()
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
				x(2).steelBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(2) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(-1).netheriteBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).netheriteBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(3) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(4) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).steelBlock()
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
				x(2).steelBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(-1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(5) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(10) {
			y(1) {
				x(-3).titaniumBlock()
				x(0).steelBlock()
				x(3).titaniumBlock()
			}
			y(2) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(-1).steelBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).steelBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(3) {
				x(-3).titaniumBlock()
				x(0).steelBlock()
				x(3).titaniumBlock()
			}
			y(0) {
				x(-2).titaniumBlock()
				x(0).steelBlock()
				x(2).titaniumBlock()
			}
			y(4) {
				x(-2).titaniumBlock()
				x(0).steelBlock()
				x(2).titaniumBlock()
			}
			y(-1) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
		}
		z(9) {
			y(1) {
				x(-3).type(Material.WAXED_COPPER_GRATE)
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).steelBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).type(Material.WAXED_COPPER_GRATE)
			}
			y(2) {
				x(-3).type(Material.WAXED_COPPER_BLOCK)
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).steelBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).steelBlock()
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).type(Material.WAXED_COPPER_BLOCK)
			}
			y(3) {
				x(-3).type(Material.WAXED_COPPER_GRATE)
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).steelBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).type(Material.WAXED_COPPER_GRATE)
			}
			y(0) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(4) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(-1) {
				x(-1).type(Material.WAXED_COPPER_GRATE)
				x(0).type(Material.WAXED_COPPER_BLOCK)
				x(1).type(Material.WAXED_COPPER_GRATE)
			}
			y(5) {
				x(-1).type(Material.WAXED_COPPER_GRATE)
				x(0).type(Material.WAXED_COPPER_BLOCK)
				x(1).type(Material.WAXED_COPPER_GRATE)
			}
		}
		z(8) {
			y(1) {
				x(-3).titaniumBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).titaniumBlock()
			}
			y(2) {
				x(-3).steelBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).netheriteBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).netheriteBlock()
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).steelBlock()
			}
			y(3) {
				x(-3).titaniumBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).titaniumBlock()
			}
			y(0) {
				x(-2).titaniumBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).titaniumBlock()
			}
			y(4) {
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
			y(5) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
		}
		z(6) {
			y(1) {
				x(-3).ironBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).ironBlock()
			}
			y(2) {
				x(-3).ironBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).netheriteBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).netheriteBlock()
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).ironBlock()
			}
			y(3) {
				x(-3).ironBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).ironBlock()
			}
			y(0) {
				x(-2).anyGlass()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).anyGlass()
			}
			y(4) {
				x(-2).anyGlass()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).anyGlass()
			}
			y(-1) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
			y(5) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
		}
		z(5) {
			y(1) {
				x(-3).ironBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).ironBlock()
			}
			y(2) {
				x(-3).type(Material.TARGET)
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).netheriteBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).netheriteBlock()
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).type(Material.TARGET)
			}
			y(3) {
				x(-3).ironBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).ironBlock()
			}
			y(0) {
				x(-2).anyGlass()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).anyGlass()
			}
			y(4) {
				x(-2).anyGlass()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).anyGlass()
			}
			y(-1) {
				x(-1).ironBlock()
				x(0).type(Material.TARGET)
				x(1).ironBlock()
			}
			y(5) {
				x(-1).ironBlock()
				x(0).type(Material.TARGET)
				x(1).ironBlock()
			}
		}
		z(4) {
			y(1) {
				x(-3).ironBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).ironBlock()
			}
			y(2) {
				x(-3).ironBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).netheriteBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).netheriteBlock()
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).ironBlock()
			}
			y(3) {
				x(-3).ironBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).ironBlock()
			}
			y(0) {
				x(-2).anyGlass()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).anyGlass()
			}
			y(4) {
				x(-2).anyGlass()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).anyGlass()
			}
			y(-1) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
			y(5) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
		}
		z(2) {
			y(1) {
				x(-3).titaniumBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).titaniumBlock()
			}
			y(2) {
				x(-3).steelBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).netheriteBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).netheriteBlock()
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).steelBlock()
			}
			y(3) {
				x(-3).titaniumBlock()
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).netheriteBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).titaniumBlock()
			}
			y(0) {
				x(-2).titaniumBlock()
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).titaniumBlock()
			}
			y(4) {
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
			y(5) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
		}
		z(1) {
			y(1) {
				x(-3).type(Material.WAXED_COPPER_GRATE)
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).steelBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).type(Material.WAXED_COPPER_GRATE)
			}
			y(2) {
				x(-3).type(Material.WAXED_COPPER_BLOCK)
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).steelBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(1).steelBlock()
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).type(Material.WAXED_COPPER_BLOCK)
			}
			y(3) {
				x(-3).type(Material.WAXED_COPPER_GRATE)
				x(-2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).steelBlock()
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(3).type(Material.WAXED_COPPER_GRATE)
			}
			y(0) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(4) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(-1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(0).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(1).customBlock(CustomBlockKeys.COPPER_COIL.getValue())
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(-1) {
				x(-1).type(Material.WAXED_COPPER_GRATE)
				x(0).type(Material.WAXED_COPPER_BLOCK)
				x(1).type(Material.WAXED_COPPER_GRATE)
			}
			y(5) {
				x(-1).type(Material.WAXED_COPPER_GRATE)
				x(0).type(Material.WAXED_COPPER_BLOCK)
				x(1).type(Material.WAXED_COPPER_GRATE)
			}
		}
		z(0) {
			y(1) {
				x(-3).titaniumBlock()
				x(0).steelBlock()
				x(3).titaniumBlock()
			}
			y(2) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(3) {
				x(-3).titaniumBlock()
				x(0).steelBlock()
				x(3).titaniumBlock()
			}
			y(0) {
				x(-2).titaniumBlock()
				x(0).steelBlock()
				x(2).titaniumBlock()
			}
			y(4) {
				x(-2).titaniumBlock()
				x(0).steelBlock()
				x(2).titaniumBlock()
			}
			y(-1) {
				x(-1).titaniumBlock()
				x(0).gridEnergyPort()
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
			}
		}
	}
}
