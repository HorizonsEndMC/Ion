package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset.stairs
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.OPPOSITE
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.SELF
import org.bukkit.block.data.Bisected.Half.BOTTOM
import org.bukkit.block.data.Bisected.Half.TOP
import org.bukkit.block.data.type.Stairs.Shape.STRAIGHT

object FluidTankMedium : FluidStorageMultiblock(75_000) {
	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-2).anyStairs(stairs(SELF, TOP, shape = STRAIGHT))
				x(-1).extractor()
				x(+0).fluidInput()
				x(+1).extractor()
				x(+2).anyStairs(stairs(SELF, TOP, shape = STRAIGHT))
			}
			y(+0) {
				x(-2).anyCopperVariant()
				x(-1).titaniumBlock()
				x(+0).copperGrate()
				x(+1).titaniumBlock()
				x(+2).anyCopperVariant()
			}
			y(+1) {
				x(-2).anyStairs(stairs(SELF, BOTTOM, shape = STRAIGHT))
				x(-1).titaniumBlock()
				x(+0).copperGrate()
				x(+1).titaniumBlock()
				x(+2).anyStairs(stairs(SELF, BOTTOM, shape = STRAIGHT))
			}
		}
		z(1) {
			y(-1) {
				x(-2).anyCopperVariant()
				x(-1).titaniumBlock()
				x(+0).copperBlock()
				x(+1).titaniumBlock()
				x(+2).anyCopperVariant()
			}
			y(+0) {
				x(-2).copperBulb()
				x(-1).anyGlass()
				x(+0).anyGlass()
				x(+1).anyGlass()
				x(+2).copperBulb()
			}
			y(+1) {
				x(-2).anyCopperVariant()
				x(-1).titaniumBlock()
				x(+0).copperGrate()
				x(+1).titaniumBlock()
				x(+2).anyCopperVariant()
			}
		}
		z(2) {
			y(-1) {
				x(-2).anyStairs(stairs(OPPOSITE, TOP, shape = STRAIGHT))
				x(-1).titaniumBlock()
				x(+0).copperGrate()
				x(+1).titaniumBlock()
				x(+2).anyStairs(stairs(OPPOSITE, TOP, shape = STRAIGHT))
			}
			y(+0) {
				x(-2).anyCopperVariant()
				x(-1).titaniumBlock()
				x(+0).copperGrate()
				x(+1).titaniumBlock()
				x(+2).anyCopperVariant()
			}
			y(+1) {
				x(-2).anyStairs(stairs(OPPOSITE, BOTTOM, shape = STRAIGHT))
				x(-1).titaniumBlock()
				x(+0).copperBlock()
				x(+1).titaniumBlock()
				x(+2).anyStairs(stairs(OPPOSITE, BOTTOM, shape = STRAIGHT))
			}
		}
	}
}
