package net.horizonsend.ion.server.features.custom

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponent
import net.horizonsend.ion.server.features.custom.items.components.LoreManager
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

open class NewCustomItem(
	val identifier: String,
	val baseItemFactory: ItemFactory,
	private val customComponents: List<CustomItemComponent>
) {
	fun constructItemStack(): ItemStack {
		val base = baseItemFactory.construct()

		base.updateMeta {
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
		}

		base.setData(DataComponentTypes.LORE, ItemLore.lore(assembleLore(base)))

		customComponents.forEach { it.decorateBase(base) }

		decorateItemStack(base)

		return base
	}

	open fun decorateItemStack(base: ItemStack) {}

	private fun assembleLore(itemStack: ItemStack): List<Component> {
		val managersSorted = customComponents.filterIsInstance<LoreManager>().sortedByDescending { it.priority }
		val iterator = managersSorted.iterator()

		val newLore = mutableListOf<Component>()

		while (iterator.hasNext()) {
			val manager = iterator.next()

			newLore.addAll(manager.getLines(itemStack))

			if (manager.shouldIncludeSeparator() && iterator.hasNext()) {
				newLore += Component.newline()
			}
		}

		return newLore
	}

	fun getAttributes(itemStack: ItemStack): List<CustomItemAttribute> = customComponents.flatMap { it.getAttributes(itemStack) }
}
