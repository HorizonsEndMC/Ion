package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.type.processing.automason.AutoMasonMultiblockEntity
import net.kyori.adventure.sound.Sound
import org.bukkit.block.Block
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class AutoMasonRecipeEnviornment(
	override val multiblock: AutoMasonMultiblockEntity,
	private val inputInventory: Inventory,
	private val outputInventory: Inventory,
	override val powerStorage: PowerStorage,
	val tickingManager: TickedMultiblockEntityParent.TickingManager
) : InventoryResultEnviornment, PoweredEnviornment {
	var wildcard: Boolean = false

	fun getCenterBlock(): Block? {
		if (wildcard) return null
		return multiblock.getBlockRelative(0, 0, 3)
	}

	override fun getInputItems(): List<ItemStack?> {
		return inputInventory.toList()
	}

	override fun getResultInventory(): Inventory {
		return outputInventory
	}

	override fun getItemSize(): Int {
		return inputInventory.size
	}

	override fun getItem(index: Int): ItemStack? {
		return inputInventory.getItem(index)
	}

	override fun playSound(sound: Sound) {
		val location = multiblock.location.toCenterLocation()
		location.world.playSound(sound, location.x, location.y, location.z)
	}
}
