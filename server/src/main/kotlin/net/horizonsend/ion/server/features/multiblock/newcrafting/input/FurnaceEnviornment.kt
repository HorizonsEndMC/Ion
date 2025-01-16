package net.horizonsend.ion.server.features.multiblock.newcrafting.input

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.newcrafting.util.SlotModificationWrapper
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

class FurnaceEnviornment(
	val furnaceInventory: FurnaceInventory,
	val powerStorage: PowerStorage,
	val tickingManager: TickedMultiblockEntityParent.TickingManager,
	val progressManager: ProgressMultiblock.ProgressManager
) : ItemResultEnviornment {
	constructor(entity: MultiblockEntity) : this(
		entity.getInventory(0, 0, 0) as FurnaceInventory,
		(entity as PoweredMultiblockEntity).powerStorage,
		(entity as TickedMultiblockEntityParent).tickingManager,
		(entity as ProgressMultiblock).progressManager
	)

	val items get() = listOf(furnaceInventory.smelting, furnaceInventory.fuel)

	override fun getItemSize(): Int = 2
	override fun getItem(index: Int): ItemStack? {
		return items[index]
	}

	override fun isEmpty(): Boolean {
		return items.all { stack -> stack == null || stack.isEmpty }
	}

	override fun getResultItem(): ItemStack? {
		return furnaceInventory.result
	}

	override fun getResultItemSlotModifier(): SlotModificationWrapper {
		return SlotModificationWrapper.furnaceResult(furnaceInventory)
	}
}
