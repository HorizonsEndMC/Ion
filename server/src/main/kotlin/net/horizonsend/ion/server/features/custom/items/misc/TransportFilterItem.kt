package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.transport.filters.FilterBlock
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FILTER_DATA
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Barrel
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.function.Supplier

class TransportFilterItem(identifier: String, displayName: Component, private val filterBlock: Supplier<FilterBlock<*>>) : CustomItem(
	identifier,
	displayName,
	ItemFactory.stackableCustomItem
) {
	fun constructItemStackB(): ItemStack {
		return ItemStack(Material.BARREL).updateMeta { meta ->
			meta as BlockStateMeta
			meta.blockState = filterBlock.get().createState()

			meta.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			meta.displayName(displayName)
		}
	}

	fun createFor(state: Barrel): ItemStack {
		return constructItemStack().updateMeta { meta ->
			meta.persistentDataContainer.set(FILTER_DATA, FilterData, state.persistentDataContainer.get(FILTER_DATA, FilterData) ?: return@updateMeta)
		}
	}

	fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		if (event == null) return
		event.isCancelled = false
	}
}
