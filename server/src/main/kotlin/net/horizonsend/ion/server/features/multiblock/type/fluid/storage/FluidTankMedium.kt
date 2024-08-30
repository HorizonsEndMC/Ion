package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape

object FluidTankMedium : FluidStorageMultiblock(75_000) {
	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).extractor()
				x(+0).fluidInput()
				x(+1).extractor()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyCopperVariant()
				x(-1).titaniumBlock()
				x(+0).copperGrate()
				x(+1).titaniumBlock()
				x(+2).anyCopperVariant()
			}
			y(+1) {
				x(-2).anyStairs()
				x(-1).titaniumBlock()
				x(+0).copperGrate()
				x(+1).titaniumBlock()
				x(+2).anyStairs()
			}
		}
		z(1) {
			y(-1) {
				x(-2).anyCopperVariant()
				x(-1).titaniumBlock()
				x(+0).anyCopperBlock()
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
				x(-2).anyStairs()
				x(-1).titaniumBlock()
				x(+0).copperGrate()
				x(+1).titaniumBlock()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyCopperVariant()
				x(-1).titaniumBlock()
				x(+0).copperGrate()
				x(+1).titaniumBlock()
				x(+2).anyCopperVariant()
			}
			y(+1) {
				x(-2).anyStairs()
				x(-1).titaniumBlock()
				x(+0).anyCopperBlock()
				x(+1).titaniumBlock()
				x(+2).anyStairs()
			}
		}
	}
}
