package net.horizonsend.ion.server.features.transport.filters.manager

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.filter.CustomFilterBlock
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.filters.FilterMeta
import net.horizonsend.ion.server.features.transport.filters.FilterType
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.util.getPersistentDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.block.TileState

abstract class FilterManager(open val manager: TransportManager<*>) {
	val filters = Long2ObjectOpenHashMap<FilterData<*, *>>()

	private val mutex = Any()

	fun getFilters(): Collection<FilterData<*, *>> {
		return filters.values
	}

	fun <T: Any, M : FilterMeta> getFilters(type: FilterType<T, M>): Collection<FilterData<T, M>> {
		return filters.values
			.filter { data -> data.type == type }
			.filterIsInstance<FilterData<T, M>>()
	}

	fun getFilter(key: BlockKey): FilterData<*, *>? {
		val saved = filters[key]
		if (saved != null) return saved

		val pdc = getPersistentDataContainer(key, manager.getWorld()) ?: return null

		val entityStored = pdc.get(NamespacedKeys.FILTER_DATA, FilterData)
		if (entityStored != null) { filters[toBlockKey(manager.getLocalCoordinate(toVec3i(key)))] = entityStored }

		return  entityStored
	}

	fun <T : Any, M : FilterMeta> getFilter(key: BlockKey, type: FilterType<T, M>): FilterData<T, M>? {
		val saved = filters[key]?.let { data -> type.cast(data) }
		if (saved != null) return saved

		val globalVec3i = manager.getGlobalCoordinate(toVec3i(key))
		val pdc = getPersistentDataContainer(globalVec3i, manager.getWorld()) ?: return null

		val entityStored = pdc.get(NamespacedKeys.FILTER_DATA, FilterData)?.let { data -> type.cast(data) }
		if (entityStored != null) { filters[toBlockKey(manager.getLocalCoordinate(toVec3i(key)))] = entityStored }

		if (entityStored == null) {
			val block = manager.getWorld().getBlockData(globalVec3i.x, globalVec3i.y, globalVec3i.z)
			val customBlock = CustomBlocks.getByBlockData(block)
			if (customBlock !is CustomFilterBlock<*, *>) return null

			@Suppress("UNCHECKED_CAST")
			return registerFilter(key, customBlock as CustomFilterBlock<T, M>)
		}

		return entityStored
	}

	fun addFilter(key: BlockKey, data: FilterData<*, *>) {
		val local = manager.getLocalCoordinate(toVec3i(key))

		synchronized(mutex) {
			filters[toBlockKey(local)] = data
		}
	}

	fun <T : Any, M : FilterMeta> registerFilter(key: BlockKey, block: CustomFilterBlock<T, M>): FilterData<T, M> {
		val global = manager.getGlobalCoordinate(toVec3i(key))
		val world = manager.getWorld()

		val state = world.getBlockState(global.x, global.y, global.z) as? TileState

		@Suppress("UNCHECKED_CAST")
		val data = state?.persistentDataContainer?.get(NamespacedKeys.FILTER_DATA, FilterData) as? FilterData<T, M> ?: block.createData(key)

		addFilter(key, data)

		return data
	}

	fun removeFilter(key: BlockKey) = synchronized(mutex) {
		val local = manager.getLocalCoordinate(toVec3i(key))
	}

	fun isFilterPresent(key: BlockKey): Boolean = synchronized(mutex) {
		return filters.containsKey(key)
	}

	abstract fun save()
	abstract fun load()

	companion object {
		fun save(tileState: TileState, data: FilterData<*, *>) {
			tileState.persistentDataContainer.set(NamespacedKeys.FILTER_DATA, FilterData, data)
			tileState.update()
		}
	}
}
