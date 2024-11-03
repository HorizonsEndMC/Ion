package net.horizonsend.ion.server.features.multiblock.old

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.GasPowerPlantMultiblock

object Multiblocks : IonServerComponent() {
	private fun initMultiblocks() {
		registerMultiblock(GasPowerPlantMultiblock)
	}

	private fun registerMultiblock(multiblock: Multiblock) {}
}
