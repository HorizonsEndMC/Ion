package net.horizonsend.ion.server.features.multiblock.newcrafting.input

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

class FurnaceEnviornment(
	val furnaceInventory: FurnaceInventory,
	val powerStorage: PowerStorage,
	val tickingManager: TickedMultiblockEntityParent.TickingManager,
	val progressManager: ProgressMultiblock.ProgressManager
) : RecipeEnviornment {
	constructor(entity: MultiblockEntity) : this(
		entity.getInventory(0, 0, 0) as FurnaceInventory,
		(entity as PoweredMultiblockEntity).powerStorage,
		(entity as TickedMultiblockEntityParent).tickingManager,
		(entity as ProgressMultiblock).progressManager
	)

	fun getProgress(): Double = progressManager.getCurrentProgress()

	override fun getItemSize(): Int = 1
	override fun getItem(index: Int): ItemStack? {
		return furnaceInventory.smelting
	}

	override fun isEmpty(): Boolean {
		return furnaceInventory.smelting != null
	}
}
