package net.horizonsend.ion.server.features.custom.items.mods.items

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.objects.CustomModeledItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.function.Supplier

class ModificationItem(
	identifier: String,

	override val customModelData: Int,
	val displayName: Component,
	val mod: Supplier<ItemModification>,
) : CustomItem(identifier), CustomModeledItem {
	override val material: Material = Material.WARPED_FUNGUS_ON_A_STICK

	override fun constructItemStack(): ItemStack {
		val itemStack = getModeledItem()

		return getModeledItem().updateMeta {
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
			it.displayName(displayName)
		}
	}
}
