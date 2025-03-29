package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory

class GasFurnaceEnviornment(
	multiblock: MultiblockEntity,
	furnaceInventory: FurnaceInventory,
	val discardInventory: Inventory,
	powerStorage: PowerStorage,
	tickingManager: TickedMultiblockEntityParent.TickingManager,
	progress: ProgressMultiblock.ProgressManager
) : FurnaceEnviornment(multiblock, furnaceInventory, powerStorage, tickingManager, progress)
