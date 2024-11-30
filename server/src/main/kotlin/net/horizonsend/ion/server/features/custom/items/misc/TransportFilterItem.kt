package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.transport.filters.FilterBlock
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FILTER_DATA
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.UUID
import java.util.function.Supplier

class TransportFilterItem(identifier: String, val displayName: Component, private val filterBlock: Supplier<FilterBlock>) : CustomItem(identifier) {
	override fun constructItemStack(): ItemStack {
		return ItemStack(Material.BARREL).updateMeta { meta ->
			meta as BlockStateMeta
			meta.blockState = filterBlock.get().createState()

			meta.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			meta.persistentDataContainer.set(FILTER_DATA, FilterData, FilterData(UUID.randomUUID()))
			meta.displayName(displayName)
		}
	}
}
