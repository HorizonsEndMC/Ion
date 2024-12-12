package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.server.features.custom.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.objects.StoredValues
import net.horizonsend.ion.server.features.custom.items.util.updateDurability
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.function.Supplier

class GasStorage(private val maxStored: Int, private val displayDurability: Boolean, val storedType: Supplier<Gas>) : CustomItemComponent, LoreManager {
	override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {
		setFill(baseItem, customItem, maxStored)
	}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = listOf()

	override val priority: Int = 25

	override fun getLines(customItem: CustomItem, itemStack: ItemStack): List<Component> {
		val stored = getFill(itemStack)
		return listOf(StoredValues.GAS.formatLore(stored, maxStored))
	}

	override fun shouldIncludeSeparator(): Boolean {
		return true
	}

	fun setFill(itemStack: ItemStack, customItem: CustomItem, newValue: Int) {
		val corrected = newValue.coerceIn(0..maxStored)

		if (displayDurability && itemStack.itemMeta is org.bukkit.entity.Damageable) {
			updateDurability(itemStack, corrected, maxStored)
		}

		customItem.refreshLore(itemStack)

		StoredValues.GAS.setAmount(itemStack, newValue)
	}

	fun getFill(itemStack: ItemStack): Int = itemStack.persistentDataContainer.getOrDefault(NamespacedKeys.GAS, PersistentDataType.INTEGER, 0).coerceIn(0, maxStored)
}
