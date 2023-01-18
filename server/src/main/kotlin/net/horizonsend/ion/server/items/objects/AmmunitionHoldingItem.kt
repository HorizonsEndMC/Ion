package net.horizonsend.ion.server.items.objects

import net.horizonsend.ion.server.NamespacedKeys.AMMO
import net.horizonsend.ion.server.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.items.CustomItem
import net.kyori.adventure.text.Component
import net.starlegacy.util.updateMeta
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.STRING

abstract class AmmunitionHoldingItem(
	identifier: String,

	private val material: Material,
	private val customModelData: Int,
	private val displayName: Component,

	private val shouldDeleteItem: Boolean = false
) : CustomItem(identifier) {
	override fun constructItemStack(): ItemStack {
		return ItemStack(material).updateMeta {
			it.setCustomModelData(customModelData)
			it.displayName(displayName)
			it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			it.persistentDataContainer.set(AMMO, INTEGER, getMaximumAmmunition())
			it.lore(listOf(Component.text("Ammo: ${getMaximumAmmunition()}/${getMaximumAmmunition()}")))
		}
	}

	protected abstract fun getMaximumAmmunition(): Int

	fun getAmmunition(itemStack: ItemStack): Int {
		// stupid undefined nullability
		return itemStack.itemMeta?.persistentDataContainer?.get(AMMO, INTEGER)?.coerceIn(0, getMaximumAmmunition()) ?: 0
	}

	open fun setAmmunition(itemStack: ItemStack, inventory: Inventory, ammunition: Int) {
		@Suppress("NAME_SHADOWING") val ammunition = ammunition.coerceIn(0, getMaximumAmmunition())

		itemStack.editMeta {
			it.lore(listOf(Component.text("Ammo: $ammunition / ${getMaximumAmmunition()}")))
			it.persistentDataContainer.set(AMMO, INTEGER, ammunition)
		}

		if (ammunition <= 0 && shouldDeleteItem) {
			inventory.removeItemAnySlot(itemStack)
		}

		(inventory.holder as? Player)?.updateInventory()
	}
}
