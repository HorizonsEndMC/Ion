package net.horizonsend.ion.server.miscellaneous.registrations.persistence

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

interface PDCSerializable <V : Any, T : PersistentDataType<PersistentDataContainer, V>> {
	val persistentDataType: T

	fun serialize(adapterContext: PersistentDataAdapterContext, data: V): PersistentDataContainer {
		return persistentDataType.toPrimitive(data, adapterContext)
	}

	fun store(destination: PersistentDataContainer, key: NamespacedKey, data: V) {
		destination.set(key, persistentDataType, data)
	}
}
