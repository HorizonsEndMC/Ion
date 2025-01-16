package net.horizonsend.ion.server.features.multiblock.newcrafting.input

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

class DoubleIndustryMultiblockEnviornment(
	val furnaceInventory: FurnaceInventory,
	val powerStorage: PowerStorage,
	val tickingManager: TickedMultiblockEntityParent.TickingManager,
	val progressManager: ProgressMultiblock.ProgressManager
) : MultiblockRecipeEnviornment {
	constructor(entity: MultiblockEntity) : this(
		entity.getInventory(0, 0, 0) as FurnaceInventory,
		(entity as PoweredMultiblockEntity).powerStorage,
		(entity as TickedMultiblockEntityParent).tickingManager,
		(entity as ProgressMultiblock).progressManager
	)

	fun getProcessedItem(): ItemStack? = furnaceInventory.smelting
	fun getProgress(): Double = progressManager.getCurrentProgress()

	override fun getItemSize(): Int = 2
	override fun getItem(index: Int): ItemStack? {
		return when (index) {
			1 -> furnaceInventory.smelting
			2 -> furnaceInventory.fuel
			else -> throw IndexOutOfBoundsException()
		}
	}

	override fun isEmpty(): Boolean {
		return furnaceInventory.smelting == null && furnaceInventory.fuel == null
	}
}
