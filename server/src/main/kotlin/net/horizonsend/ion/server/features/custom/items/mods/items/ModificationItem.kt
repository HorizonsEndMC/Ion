package net.horizonsend.ion.server.features.custom.items.mods.items

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.objects.CustomModeledItem
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ModificationItem(
	identifier: String,

	override val customModelData: Int,
	val displayName: Component,
	val mod: ItemModification
) : CustomItem(identifier), CustomModeledItem {
	override val material: Material = Material.WARPED_FUNGUS_ON_A_STICK

	override fun constructItemStack(): ItemStack {
		val itemStack = getModeledItem()

		return getModeledItem().updateMeta {
			it.displayName(displayName)
		}
	}
}
