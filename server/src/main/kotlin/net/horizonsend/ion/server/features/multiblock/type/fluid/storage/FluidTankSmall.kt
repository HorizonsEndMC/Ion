package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape

object FluidTankSmall : FluidStorageMultiblock(25_000) {
	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).fluidInput()
				x(+1).anyStairs()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).extractor()
				x(+1).anyStairs()
			}
		}
		z(1) {
			y(-1) {
				x(-1).titaniumBlock()
				x(+0).anyCopperGrate()
				x(+1).titaniumBlock()
			}
			y(+0) {
				x(-1).titaniumBlock()
				x(+0).anyCopperGrate()
				x(+1).titaniumBlock()
			}
		}
		z(2) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).anyCopperVariant()
				x(+1).anyStairs()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).anyCopperVariant()
				x(+1).anyStairs()
			}
		}
	}
}
