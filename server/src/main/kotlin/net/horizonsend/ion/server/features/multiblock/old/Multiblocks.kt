package net.horizonsend.ion.server.features.multiblock.old

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.AmmoLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.MissileLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.StandardAmmoPressMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.GasPowerPlantMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.VentMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.collector.GasCollectorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CircuitfabMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CompressorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.FabricatorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.GasFurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.PlatePressMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.ItemSplitterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace.PowerFurnaceMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace.PowerFurnaceMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace.PowerFurnaceMultiblockTier3

object Multiblocks : IonServerComponent() {
	private fun initMultiblocks() {
		// Recipe system multis
		registerMultiblock(CentrifugeMultiblock)
		registerMultiblock(CompressorMultiblock)
		registerMultiblock(FabricatorMultiblock)
		registerMultiblock(CircuitfabMultiblock)
		registerMultiblock(PlatePressMultiblock)
		registerMultiblock(GasFurnaceMultiblock)
		registerMultiblock(MissileLoaderMultiblock)
		registerMultiblock(AmmoLoaderMultiblock)

		// Furnace start
		registerMultiblock(PowerFurnaceMultiblockTier1)
		registerMultiblock(PowerFurnaceMultiblockTier2)
		registerMultiblock(PowerFurnaceMultiblockTier3)

		registerMultiblock(StandardAmmoPressMultiblock)

		registerMultiblock(GasCollectorMultiblock)
		registerMultiblock(VentMultiblock)
		registerMultiblock(GasPowerPlantMultiblock)
		// Furnace end

		registerMultiblock(ItemSplitterMultiblock)
	}

	private fun registerMultiblock(multiblock: Multiblock) {}
}
