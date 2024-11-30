package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.CUSTOM_ITEM
import org.bukkit.Material
import org.bukkit.block.Barrel
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.function.Supplier

object FilterBlocks : IonServerComponent() {
	private val filters = mutableMapOf<String, FilterBlock>()
	fun all(): Map<String, FilterBlock> = filters

	fun getFilterBlock(state: Barrel): FilterBlock? {
		val identifier = state.persistentDataContainer.get(CUSTOM_ITEM, STRING) ?: return null
		return filters[identifier]
	}

	val FLUID_FILTER: FilterBlock = registerFilter("FLUID_FILTER", CacheType.FLUID) { CustomItems.FLUID_FILTER }

	private fun registerFilter(identifier: String, cacheType: CacheType, customItemSupplier: Supplier<CustomItem>): FilterBlock {
		val data = FilterBlock(identifier, cacheType, customItemSupplier)
		filters[identifier] = data
		return data
	}
}

class FilterBlock(val identifier: String, val cacheType: CacheType, private val customItemSupplier: Supplier<CustomItem>) {
	val customItem get() = customItemSupplier.get()

	fun createState(): Barrel {
		val baseState = FILTER_MATERIAL.createBlockData().createBlockState() as Barrel
		baseState.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
		return baseState
	}

	fun openGUI(player: Player, data: FilterData) {
		println("$player $data")
	}

	companion object {
		val FILTER_MATERIAL = Material.BARREL
		val FILTER_STATE_TYPE = Barrel::class
	}
}
