package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER

class StorageContainer(
	val name: String,
	val displayName: Component,
	val namespacedKey: NamespacedKey,
	val storage: InternalStorage
) {
	fun save(destination: PersistentDataContainer) {
		val pdc = destination.adapterContext.newPersistentDataContainer()
		storage.saveData(pdc)

		destination.set(namespacedKey, TAG_CONTAINER, pdc)
	}

	override fun toString(): String {
		return "Container[name= $name, key= $namespacedKey, storage= $storage]"
	}
}
