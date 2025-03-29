package net.horizonsend.ion.server.features.custom.items.type.tool.mods

import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bukkit.inventory.ItemStack

class ModificationItem(
	key: IonRegistryKey<CustomItem, out CustomItem>,
	val modKey: IonRegistryKey<ItemModification, out ItemModification>,

	val model: String,
	displayName: Component,
	vararg description: Component,
) : CustomItem(
	key,
	displayName,
	ItemFactory
		.builder(ItemFactory.unStackableCustomItem)
		.setCustomModel(model)
		.build()
) {
	private val descriptionLines = Array(description.size) { description[it].itemLore }

	override fun assembleLore(itemStack: ItemStack): List<Component> {
		val applicableTo = CustomItemKeys.allkeys()
			.filter { customItem ->
				modKey.getValue().applicationPredicates.any { predicate -> predicate.canApplyTo(customItem.getValue()) }
			}
			.map { customItem -> customItem.getValue().displayName.itemLore }
			.toTypedArray()

		return mutableListOf(
			*descriptionLines,
			empty(),
			text("Applicable to:", HE_MEDIUM_GRAY).decoration(ITALIC, false),
			*applicableTo
		)
	}
}
