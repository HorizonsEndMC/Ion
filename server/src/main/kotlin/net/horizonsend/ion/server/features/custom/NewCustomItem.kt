package net.horizonsend.ion.server.features.custom

import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponent
import net.horizonsend.ion.server.features.custom.items.components.LoreManager
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.data.DataVersioned
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

open class NewCustomItem(
	val identifier: String,
	val displayName: Component,
	baseItemFactory: ItemFactory,
	val customComponents: List<CustomItemComponent> = listOf(),
	override val latestDataVersion: Int = 0
) : DataVersioned<ItemStack> {
	protected val baseItemFactory = ItemFactory.builder(baseItemFactory)
		.setNameSupplier { displayName.itemName }
		.addPDCEntry(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
		.addModifier { base -> customComponents.forEach { it.decorateBase(base) } }
		.addModifier { base -> decorateItemStack(base) }
		.setLoreSupplier { base -> assembleLore(base) }
		.build()

	fun constructItemStack(): ItemStack = baseItemFactory.construct()

	protected open fun decorateItemStack(base: ItemStack) {}

	private fun assembleLore(itemStack: ItemStack): List<Component> {
		val managersSorted = customComponents.filterIsInstance<LoreManager>().sortedByDescending { it.priority }
		val iterator = managersSorted.iterator()

		val newLore = mutableListOf<Component>()

		while (iterator.hasNext()) {
			val manager = iterator.next()

			newLore.addAll(manager.getLines(this, itemStack))

			if (manager.shouldIncludeSeparator() && iterator.hasNext()) {
				newLore += Component.newline()
			}
		}

		return newLore
	}

	fun getAttributes(itemStack: ItemStack): List<CustomItemAttribute> = customComponents.flatMap { it.getAttributes(itemStack) }

	override fun getDataVersion(subject: ItemStack): Int {
		return subject.persistentDataContainer.getOrDefault(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, 0)
	}

	override fun setDataVersion(subject: ItemStack, version: Int) {
		subject.itemMeta.persistentDataContainer.set(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, version)
	}
}
