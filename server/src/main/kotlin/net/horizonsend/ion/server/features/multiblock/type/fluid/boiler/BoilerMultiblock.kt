package net.horizonsend.ion.server.features.multiblock.type.fluid.boiler

import net.horizonsend.ion.server.features.multiblock.Multiblock

abstract class BoilerMultiblock : Multiblock() {
	override val name: String = "boiler"

	abstract class BoilerMultiblockEntity() {

	}
}
