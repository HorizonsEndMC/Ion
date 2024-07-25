package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.objects.CustomModeledItem
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.function.Supplier

class ModificationItem(
	identifier: String,

	override val customModelData: Int,
	val displayName: Component,
	vararg description: Component,
	private val modSupplier: Supplier<ItemModification>,
) : CustomItem(identifier), CustomModeledItem {
	override val material: Material = Material.WARPED_FUNGUS_ON_A_STICK

	private val descriptionLines = arrayOf(*description)

	/** The tool modification this item represents */
	val modification get() = modSupplier.get()

	override fun constructItemStack(): ItemStack = getModeledItem().updateMeta {
		val applicableTo = CustomItems.ALL
			.filterIsInstance<ModdedCustomItem>()
			.filter { customItem ->
				modification.applicableTo.contains(customItem::class)
			}
			.map { customItem -> customItem.displayName }
			.toTypedArray()

		it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
		it.lore(mutableListOf(
			*descriptionLines,
			empty(),
			text("Applicable to:", HE_MEDIUM_GRAY).decoration(ITALIC, false),
			*applicableTo
		))
		it.displayName(displayName)
	}
}
