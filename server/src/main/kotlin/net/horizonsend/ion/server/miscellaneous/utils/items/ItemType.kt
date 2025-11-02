package net.horizonsend.ion.server.miscellaneous.utils.items

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

sealed interface ItemType {
	fun matches(itemStack: ItemStack): Boolean

	class SimpleMaterial(val material: Material) : ItemType {
		override fun matches(itemStack: ItemStack): Boolean {
			return itemStack.type == material
		}
	}
	class CustomItem(val key: IonRegistryKey<CustomItem, out CustomItem>) : ItemType {
		override fun matches(itemStack: ItemStack): Boolean {
			return itemStack.customItem?.key == key
		}
	}
	class ExactMatch(val stack: ItemStack) : ItemType {
		override fun matches(itemStack: ItemStack): Boolean {
			return stack.isSimilar(itemStack)
		}
	}
}
