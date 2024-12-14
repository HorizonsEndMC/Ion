package net.horizonsend.ion.server.features.custom.items

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponent
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager.ComponentTypeData
import net.horizonsend.ion.server.features.custom.items.component.LoreManager
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.custom.items.util.serialization.SerializationManager
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.invui.item.ItemProvider

open class CustomItem(
	val identifier: String,
	val displayName: Component,
	baseItemFactory: ItemFactory,
) : ItemProvider {
	protected val serializationManager: SerializationManager = SerializationManager()
	open val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager)

	fun allComponents() = customComponents.getAll()

	/**
	 * Gets the component of the specified type. Throws exception if not present. Use #hasComponent to check beforehand
	 **/
	fun <T : CustomItemComponent> getComponent(type: CustomComponentTypes<T, ComponentTypeData.OnlyOne<T>>): T {
		return customComponents.getComponent(type)
	}

	/**
	 * Gets the components of the specified type. Throws exception if not present. Use #hasComponent to check beforehand
	 **/
	fun <T : CustomItemComponent> getComponents(type: CustomComponentTypes<T, ComponentTypeData.AllowMultiple<T>>): List<T> {
		return customComponents.getComponents(type)
	}

	/**
	 * Returns whether this item has the specified component
	 **/
	fun hasComponent(type: CustomComponentTypes<*, *>): Boolean = customComponents.hasComponent(type)

	protected open val baseItemFactory = ItemFactory.builder(baseItemFactory)
		.setNameSupplier { displayName.itemName }
		.addPDCEntry(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
		.addModifier { base -> customComponents.getAll().forEach { it.decorateBase(base, this) } }
		.addModifier { base -> decorateItemStack(base) }
		.setLoreSupplier { base -> assembleLore(base) }
		.build()

	fun constructItemStack(): ItemStack = try { baseItemFactory.construct() } catch (e: Throwable) { throw Throwable("Error when constructing custom item $identifier", e) }

	override fun get(localization: String?): ItemStack {
		return constructItemStack()
	}

	fun constructItemStack(quantity: Int): ItemStack {
		val constructed = baseItemFactory.construct()
		val maxSize = constructed.getData(DataComponentTypes.MAX_STACK_SIZE) ?:
			constructed.type.asItemType()?.getDefaultData(DataComponentTypes.MAX_STACK_SIZE) ?: 1

		return constructed.asQuantity(quantity.coerceIn(1..maxSize))
	}

	protected open fun decorateItemStack(base: ItemStack) {}

	open fun assembleLore(itemStack: ItemStack): List<Component> {
		val managersSorted = customComponents.getAll().filterIsInstance<LoreManager>().sortedByDescending { it.priority }
		val iterator = managersSorted.iterator()

		val newLore = mutableListOf<Component>()

		while (iterator.hasNext()) {
			val manager = iterator.next()

			newLore.addAll(manager.getLines(this, itemStack))

			if (manager.shouldIncludeSeparator() && iterator.hasNext()) {
				newLore += Component.empty()
			}
		}

		return newLore
	}

	fun refreshLore(itemStack: ItemStack) {
		itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(assembleLore(itemStack)))
	}

	fun getAttributes(itemStack: ItemStack): List<CustomItemAttribute> = customComponents.getAll().flatMap { it.getAttributes(itemStack) }

	fun serialize(itemStack: ItemStack) = serializationManager.serialize(this, itemStack)
	fun deserialize(data: String) = serializationManager.deserialize(this, data)

	fun getParamaterKeys() = serializationManager.parameterKeys()
}
