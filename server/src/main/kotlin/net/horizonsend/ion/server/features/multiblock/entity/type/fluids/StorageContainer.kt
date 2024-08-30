package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer

class StorageContainer(
	val name: String,
	val displayName: Component,
	private val namespacedKey: NamespacedKey,
	val storage: InternalStorage
) {
	fun save(destination: PersistentDataContainer) {
		destination.set(namespacedKey, InternalStorage, storage)
	}
}
