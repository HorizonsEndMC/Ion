package net.horizonsend.ion.server.features.custom.items.objects

import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface CustomModeledItem {
	val material: Material
	val customModelData: Int

	fun getModeledItem(): ItemStack {
		val itemStack = ItemStack(material)

		return itemStack.updateMeta {
			it.setCustomModelData(customModelData)
		}
	}
}
