package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.server.features.custom.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import java.util.function.Supplier

class ModificationItem(
	identifier: String,

	val model: String,
	displayName: Component,
	vararg description: Component,
	private val modSupplier: Supplier<ItemModification>,
) : NewCustomItem(
	identifier,
	displayName,
	ItemFactory
		.builder(ItemFactory.unStackableCustomItem)
		.setCustomModel(model)
		.setLoreSupplier {
			val applicableTo = CustomItemRegistry.ALL
				.filter { customItem ->
					modSupplier.get().applicableTo.contains(customItem::class)
				}
				.map { customItem -> customItem.displayName }
				.toTypedArray()

			mutableListOf(
				*description,
				empty(),
				text("Applicable to:", HE_MEDIUM_GRAY).decoration(ITALIC, false),
				*applicableTo
			)
		}
		.build()
) {
	private val descriptionLines = arrayOf(*description)

	/** The tool modification this item represents */
	val modification get() = modSupplier.get()
}
