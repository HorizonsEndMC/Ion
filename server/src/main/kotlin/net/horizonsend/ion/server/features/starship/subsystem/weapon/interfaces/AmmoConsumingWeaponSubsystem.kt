package net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface AmmoConsumingWeaponSubsystem {
	fun isRequiredAmmo(item: ItemStack): Boolean
	fun consumeAmmo(itemStack: ItemStack)

	fun requireMaterial(itemStack: ItemStack, material: Material, amount: Int): Boolean {
		if (itemStack.isEmpty) return false
		return itemStack.type == material && itemStack.amount >= amount
	}

	fun requireCustomItem(itemStack: ItemStack, customItem: CustomItem, amount: Int): Boolean {
		if (itemStack.isEmpty) return false
		return itemStack.customItem == customItem && itemStack.amount >= amount
	}

	fun consumeItem(itemStack: ItemStack, amount: Int) {
		itemStack.amount -= amount
	}
}
