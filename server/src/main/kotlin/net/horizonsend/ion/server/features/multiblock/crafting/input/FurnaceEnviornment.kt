package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.crafting.util.SlotModificationWrapper
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.kyori.adventure.sound.Sound
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

open class FurnaceEnviornment(
	override val multiblock: MultiblockEntity,
	val furnaceInventory: FurnaceInventory,
	override val powerStorage: PowerStorage,
	val tickingManager: TickedMultiblockEntityParent.TickingManager,
	val progress: ProgressMultiblock.ProgressManager
) : SingleItemResultEnviornment, ProgressEnviornment, PoweredEnviornment {
	constructor(entity: MultiblockEntity) : this(
		entity,
		entity.getInventory(0, 0, 0) as FurnaceInventory,
		(entity as PoweredMultiblockEntity).powerStorage,
		(entity as TickedMultiblockEntityParent).tickingManager,
		(entity as ProgressMultiblock).progressManager
	)

	override fun getInputItems(): List<ItemStack?> = listOf(furnaceInventory.smelting, furnaceInventory.fuel)

	override fun getItemSize(): Int = 2
	override fun getItem(index: Int): ItemStack? {
		return getInputItems()[index]
	}

	override fun isEmpty(): Boolean {
		return getInputItems().all { stack -> stack == null || stack.isEmpty }
	}

	override fun getResultItem(): ItemStack? {
		return furnaceInventory.result
	}

	override fun getResultItemSlotModifier(): SlotModificationWrapper {
		return SlotModificationWrapper.furnaceResult(furnaceInventory)
	}

	override fun getProgressManager(): ProgressMultiblock.ProgressManager = progress

	override fun playSound(sound: Sound) {
		val furnaceLocation = furnaceInventory.holder?.location?.toCenterLocation()
			?: throw IllegalStateException("Virtual furnace inventory passed to multiblock recipe! Don't do that!")

		furnaceLocation.world.playSound(sound, furnaceLocation.x, furnaceLocation.y, furnaceLocation.z)
	}
}
