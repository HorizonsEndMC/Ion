package net.horizonsend.ion.server.features.multiblock.old

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.StandardAmmoPressMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.GasPowerPlantMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.VentMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.collector.GasCollectorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.ItemSplitterMultiblock

object Multiblocks : IonServerComponent() {
	private fun initMultiblocks() {
		registerMultiblock(StandardAmmoPressMultiblock)

		registerMultiblock(GasCollectorMultiblock)
		registerMultiblock(VentMultiblock)
		registerMultiblock(GasPowerPlantMultiblock)
		// Furnace end

		registerMultiblock(ItemSplitterMultiblock)
	}

	private fun registerMultiblock(multiblock: Multiblock) {}
}
