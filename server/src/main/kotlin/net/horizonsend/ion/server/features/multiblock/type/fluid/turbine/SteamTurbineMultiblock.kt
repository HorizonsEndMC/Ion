package net.horizonsend.ion.server.features.multiblock.type.fluid.turbine

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape

object SteamTurbineMultiblock : TurbineMultiblock() {
	override val maximumSteamConsumptionPerSecond: Double = 7.4
	override val maximumPowerGenerationPerSecond: Double = 110.0
	override val steamInputCapacity: Double = 1_000_000.0

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(-1).extractor()
				x(0).ironBlock()
				x(1).extractor()
				x(-1).fluidInput()
				x(0).anyCopperBulb()
				x(1).fluidInput()
			}
			y(-1) {
				x(-1).fluidInput()
				x(-1).extractor()
				x(0).powerInput()
				x(1).fluidInput()
				x(1).extractor()
			}
		}
		z(1) {
			y(-1) {
				x(-2).lightningRod()
				x(-1).anyCopperGrate()
				x(0).steelBlock()
				x(0).terracottaOrDoubleSlab()
				x(1).anyCopperGrate()
				x(2).lightningRod()
			}
			y(0) {
				x(-1).ironBlock()
				x(0).steelBlock()
				x(1).ironBlock()
				x(-1).titaniumBlock()
				x(0).ironBlock()
				x(1).titaniumBlock()
			}
			y(1) {
				x(0).titaniumBlock()
			}
		}
		z(2) {
			y(-1) {
				x(-2).lightningRod()
				x(-1).anyCopperGrate()
				x(0).steelBlock()
				x(0).terracottaOrDoubleSlab()
				x(1).anyCopperGrate()
				x(2).lightningRod()
			}
			y(0) {
				x(-2).ironBlock()
				x(-1).steelBlock()
				x(0).anyCopperBulb()
				x(1).steelBlock()
				x(2).ironBlock()
				x(-2).titaniumBlock()
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(2).titaniumBlock()
			}
			y(1) {
				x(-2).ironBlock()
				x(-2).titaniumBlock()
				x(0).ironBlock()
				x(2).ironBlock()
				x(2).titaniumBlock()
			}
			y(2) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
		}
		z(3) {
			y(-1) {
				x(-2).lightningRod()
				x(-1).anyCopperGrate()
				x(0).steelBlock()
				x(0).terracottaOrDoubleSlab()
				x(1).anyCopperGrate()
				x(2).lightningRod()
			}
			y(0) {
				x(-1).ironBlock()
				x(0).steelBlock()
				x(1).ironBlock()
				x(-1).titaniumBlock()
				x(0).ironBlock()
				x(1).titaniumBlock()
			}
			y(1) {
				x(0).titaniumBlock()
			}
		}
		z(4) {
			y(-1) {
				x(-2).lightningRod()
				x(-1).anyCopperGrate()
				x(0).steelBlock()
				x(0).terracottaOrDoubleSlab()
				x(1).anyCopperGrate()
				x(2).lightningRod()
			}
			y(0) {
				x(-2).ironBlock()
				x(-1).steelBlock()
				x(0).anyCopperBulb()
				x(1).steelBlock()
				x(2).ironBlock()
				x(-2).titaniumBlock()
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(2).titaniumBlock()
			}
			y(1) {
				x(-2).ironBlock()
				x(-2).titaniumBlock()
				x(0).ironBlock()
				x(2).ironBlock()
				x(2).titaniumBlock()
			}
			y(2) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
		}
		z(5) {
			y(-1) {
				x(-2).lightningRod()
				x(-1).anyCopperGrate()
				x(0).steelBlock()
				x(0).terracottaOrDoubleSlab()
				x(1).anyCopperGrate()
				x(2).lightningRod()
			}
			y(0) {
				x(-1).ironBlock()
				x(0).steelBlock()
				x(1).ironBlock()
				x(-1).titaniumBlock()
				x(0).ironBlock()
				x(1).titaniumBlock()
			}
			y(1) {
				x(0).titaniumBlock()
			}
		}
		z(6) {
			y(-1) {
				x(0).terracottaOrDoubleSlab()
			}
			y(0) {
				x(0).anyCopperBulb()
			}
		}
	}
}
