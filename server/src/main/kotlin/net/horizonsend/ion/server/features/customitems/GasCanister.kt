package net.horizonsend.ion.server.features.customitems

import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType
import kotlin.math.roundToInt

abstract class GasCanister(
	identifier: String,

	val maximumFill: Int,
	private val customModelData: Int,
	val displayName: Component,
	val gasIdentifier: String
) : CustomItem(identifier) {
	val gas = Gasses[gasIdentifier]!!

	override fun constructItemStack(): ItemStack {
		return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
			it.setCustomModelData(customModelData)
			it.displayName(displayName)
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
			it.persistentDataContainer.set(NamespacedKeys.GAS, PersistentDataType.INTEGER, maximumFill)
			it.lore(listOf(lore(maximumFill, maximumFill)))
		}
	}

	fun setFill(itemStack: ItemStack, inventory: Inventory, newValue: Int) {
		itemStack.editMeta {
			it.persistentDataContainer.set(NamespacedKeys.GAS, PersistentDataType.INTEGER, newValue)
			it.lore(listOf(lore(maximumFill, newValue)))
			it.isUnbreakable = false
			(it as Damageable).damage = (itemStack.type.maxDurability - newValue.toDouble() / maximumFill * itemStack.type.maxDurability).roundToInt()
		}

		if (newValue <= 0) {
			inventory.removeItemAnySlot(itemStack)
		}
	}

	fun getFill(itemStack: ItemStack): Int =
		itemStack.itemMeta?.persistentDataContainer?.get(NamespacedKeys.GAS, PersistentDataType.INTEGER)?.coerceIn(0, maximumFill) ?: 0


	/** Replaces the gas canister with an empty one **/
	fun empty(itemStack: ItemStack, inventory: Inventory) {
		val empty = CustomItems.EMPTY_GAS_CANISTER.constructItemStack()

		val firstMatching = inventory.all(itemStack).keys.firstOrNull() ?: return // Shouldn't happen

		inventory.setItem(firstMatching, empty)

		(inventory.holder as? Player)?.updateInventory()
	}

	companion object {
		private fun lore(maximum: Int, fill: Int) = text()
			.decoration(TextDecoration.ITALIC, false)
			.append(text("Gas: ", NamedTextColor.GRAY))
			.append(text(maximum, NamedTextColor.AQUA))
			.append(text(" / ", NamedTextColor.GRAY))
			.append(text(fill, NamedTextColor.AQUA))
			.build()
	}
}
