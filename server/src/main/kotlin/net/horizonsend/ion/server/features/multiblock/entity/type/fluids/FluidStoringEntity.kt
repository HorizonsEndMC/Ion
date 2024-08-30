package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.STORAGES
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER

interface FluidStoringEntity {
	val capacities: Array<StorageContainer>

	fun canStore(fluid: PipedFluid, amount: Int) = capacities.any { it.storage.canStore(fluid, amount) }

	fun firstCasStore(fluid: PipedFluid, amount: Int): StorageContainer? = capacities.firstOrNull { it.storage.canStore(fluid, amount) }

	fun getStoredResources() : Map<PipedFluid?, Int> = capacities.associate { it.storage.getStoredFluid() to it.storage.getAmount() }

	fun getNamedStorage(name: String): StorageContainer = capacities.first { it.name == name }

	fun getStorage(key: NamespacedKey): StorageContainer = capacities.first { it.namespacedKey == key }


	fun storeStorageData(destination: PersistentDataContainer, context: PersistentDataAdapterContext) {
		val storages = context.newPersistentDataContainer()

		capacities.forEach {
			it.save(storages)
		}

		destination.set(STORAGES, TAG_CONTAINER, storages)
	}

	/**
	 * Loads the defined resource storage container from the configuration, or constructs a new one with the information provided.
	 **/
	fun loadStoredResource(
		data: PersistentMultiblockData,
		name: String,
		displayName: Component,
		namespacedKey: NamespacedKey,
		internalStorage: InternalStorage
	): StorageContainer {
		val storages = data.getAdditionalData(STORAGES, TAG_CONTAINER)

		storages?.get(namespacedKey, TAG_CONTAINER)?.let {
			internalStorage.loadData(it)
		}

		return StorageContainer(
			name,
			displayName,
			namespacedKey,
			internalStorage
		)
	}
}
