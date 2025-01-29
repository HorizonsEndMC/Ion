package net.horizonsend.ion.server.features.transport.filters.manager

import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.manager.ChunkTransportManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class ChunkFilterManager(override val manager: ChunkTransportManager) : FilterManager(manager) {
	override fun save() {
		val pdc = manager.chunk.inner.persistentDataContainer

		val filterData = mutableListOf<PersistentDataContainer>()

		for (entry in filters.values) {
			filterData.add(FilterData.toPrimitive(entry, pdc.adapterContext))
		}

		pdc.set(NamespacedKeys.CHUNK_FILTER_DATA, PersistentDataType.LIST.dataContainers(), filterData)
	}

	override fun load() {
		val pdc = manager.chunk.inner.persistentDataContainer
		val stored = pdc.getOrDefault(NamespacedKeys.CHUNK_FILTER_DATA, PersistentDataType.LIST.dataContainers(), listOf())

		for (data in stored) {
			val filterData = FilterData.fromPrimitive(data, pdc.adapterContext)
			filters[filterData.position] = filterData
		}
	}
}
