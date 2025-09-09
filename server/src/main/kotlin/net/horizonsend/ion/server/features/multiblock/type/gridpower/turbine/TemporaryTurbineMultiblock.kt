package net.horizonsend.ion.server.features.multiblock.type.gridpower.turbine

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape

object TemporaryTurbineMultiblock : TurbineMultiblock() {
	override fun MultiblockShape.buildStructure() {
		z(1) {
			y(0) {
				x(3).fluidPort()
				x(-3).fluidPort()
			}
		}
		z(0) {
			y(-3) {
				x(1).steelBlock()
				x(0).steelBlock()
				x(-1).steelBlock()
			}
			y(-2) {
				x(2).steelBlock()
				x(0).ironBlock()
				x(-2).steelBlock()
			}
			y(-1) {
				x(3).steelBlock()
				x(0).ironBlock()
				x(-3).steelBlock()
			}
			y(0) {
				x(3).steelBlock()
				x(2).ironBlock()
				x(1).ironBlock()
				x(0).customBlock(CustomBlockKeys.ROTATION_SHAFT.getValue())
				x(-1).ironBlock()
				x(-2).ironBlock()
				x(-3).steelBlock()
			}
			y(1) {
				x(3).steelBlock()
				x(0).ironBlock()
				x(-3).steelBlock()
			}
			y(2) {
				x(2).steelBlock()
				x(0).ironBlock()
				x(-2).steelBlock()
			}
			y(3) {
				x(1).steelBlock()
				x(0).steelBlock()
				x(-1).steelBlock()
			}
		}
	}
}
