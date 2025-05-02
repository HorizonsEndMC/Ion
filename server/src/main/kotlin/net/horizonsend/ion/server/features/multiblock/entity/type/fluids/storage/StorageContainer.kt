package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER

/**
 * A wrapper around the internal storage that contains information for displaying and saving the resources.
 **/
class StorageContainer(
	val name: String,
	val displayName: Component,
	val namespacedKey: NamespacedKey,
	val internalStorage: InternalStorage
) {
	fun save(destination: PersistentDataContainer) {
		val pdc = destination.adapterContext.newPersistentDataContainer()
		internalStorage.saveData(pdc)

		destination.set(namespacedKey, TAG_CONTAINER, pdc)
	}

	override fun toString(): String {
		return "Container[name= $name, key= $namespacedKey, storage= $internalStorage]"
	}
}
