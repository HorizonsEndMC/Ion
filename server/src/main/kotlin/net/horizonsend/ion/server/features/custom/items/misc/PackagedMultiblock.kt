package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING

object PackagedMultiblock : CustomItem("PACKAGED_MULTIBLOCK") {
	override fun constructItemStack(): ItemStack {
		return ItemStack(Material.CHEST).updateMeta {
			it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
		}
	}

	fun createFor() {
		
	}
}
