package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.misc.TransportFilterItem
import net.horizonsend.ion.server.features.transport.fluids.Fluid
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FILTER_DATA
import org.bukkit.Material
import org.bukkit.block.Barrel
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.function.Supplier

object FilterBlocks : IonServerComponent() {
	private val filters = mutableMapOf<String, FilterBlock<*>>()
	fun all(): Map<String, FilterBlock<*>> = filters

	fun getFilterBlock(state: Barrel): FilterBlock<*>? {
		val identifier = state.persistentDataContainer.get(CUSTOM_ITEM, STRING) ?: return null
		return filters[identifier]
	}

	val FLUID_FILTER: FilterBlock<Fluid> = registerFilter("FLUID_FILTER", CacheType.FLUID) { CustomItemRegistry.FLUID_FILTER }

	private fun <T: Any> registerFilter(identifier: String, cacheType: CacheType, customItemSupplier: Supplier<TransportFilterItem>): FilterBlock<T> {
		val data = FilterBlock<T>(identifier, cacheType, customItemSupplier)
		filters[identifier] = data
		return data
	}
}

class FilterBlock<T: Any>(val identifier: String, val cacheType: CacheType, private val customItemSupplier: Supplier<TransportFilterItem>) {
	val customItem get() = customItemSupplier.get()

	fun createState(): Barrel {
		val baseState = FILTER_MATERIAL.createBlockData().createBlockState() as Barrel
		baseState.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
		return baseState
	}

	fun openGUI(player: Player, state: Barrel, data: FilterData<*>?) {
		if (data != null) {
			state.persistentDataContainer.set(FILTER_DATA, FilterData, FilterData<T>(false, mutableListOf()))
		}

		println("$player $data")
	}

	companion object {
		val FILTER_MATERIAL = Material.BARREL
		val FILTER_STATE_TYPE = Barrel::class
	}
}
