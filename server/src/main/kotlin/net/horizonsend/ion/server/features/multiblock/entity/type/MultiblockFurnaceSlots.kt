package net.horizonsend.ion.server.features.multiblock.entity.type

import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

// To add to this slot defining config find the "override val name = "multiblockname"" inside a multiblock.kt
object MultiblockFurnaceSlots {
	val primaryInputSlots: Map<String, FurnaceInputSlot> = mapOf(
		"centrifuge" to FurnaceInputSlot.TOP,
		"gascollector" to FurnaceInputSlot.BOTTOM
	)
}

enum class FurnaceInputSlot {
	TOP,
	BOTTOM;

	fun get(inventory: FurnaceInventory): ItemStack? = when (this) {
		TOP -> inventory.smelting
		BOTTOM -> inventory.fuel
	}

	fun set(inventory: FurnaceInventory, item: ItemStack?) {
		when (this) {
			TOP -> inventory.smelting = item
			BOTTOM -> inventory.fuel = item
		}
	}
}
