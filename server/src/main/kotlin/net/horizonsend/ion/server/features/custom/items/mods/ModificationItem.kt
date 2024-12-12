package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.server.features.custom.CustomItem
import net.horizonsend.ion.server.features.custom.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class ModificationItem(
	identifier: String,

	val model: String,
	displayName: Component,
	vararg description: Component,
	private val modSupplier: Supplier<ItemModification>,
) : CustomItem(
	identifier,
	displayName,
	ItemFactory
		.builder(ItemFactory.unStackableCustomItem)
		.setCustomModel(model)
		.build()
) {
	private val descriptionLines = arrayOf(*description)

	override fun assembleLore(itemStack: ItemStack): List<Component> {
		val applicableTo = CustomItemRegistry.ALL
			.filter { customItem ->
				modSupplier.get().applicableTo.contains(customItem::class)
			}
			.map { customItem -> customItem.displayName }
			.toTypedArray()

		return mutableListOf(
			*descriptionLines,
			empty(),
			text("Applicable to:", HE_MEDIUM_GRAY).decoration(ITALIC, false),
			*applicableTo
		)
	}

	/** The tool modification this item represents */
	val modification get() = modSupplier.get()
}
