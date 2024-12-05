package net.horizonsend.ion.server.features.custom

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.unStackableCustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.withModel
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.kyori.adventure.text.Component.text
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING

object CustomItemRegistry : IonServerComponent() {
	private val customItems = mutableMapOf<String, NewCustomItem>()
	val ALL get() = customItems.values

	val GUN_BARREL = register(NewCustomItem("GUN_BARREL", text("Gun Barrel"), unStackableCustomItem.withModel("items/gun_barrel")))
	val CIRCUITRY = register(NewCustomItem("CIRCUITRY", text("Circuitry"), unStackableCustomItem.withModel("items/circuitry")))

	fun <T : NewCustomItem> register(item: T): T {
		customItems[item.identifier] = item
		return item
	}

	val ItemStack.newCustomItem: NewCustomItem? get() {
		return customItems[persistentDataContainer.get(CUSTOM_ITEM, STRING) ?: return null]
	}

	val identifiers = customItems.keys

	fun getByIdentifier(identifier: String): NewCustomItem? = customItems[identifier]
}
