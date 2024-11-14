package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.InternalStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.STORAGES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER

interface FluidStoringEntity {
	val capacities: Array<StorageContainer>
	val fluidInputOffsets: Array<Vec3i>

	fun getFluidInputLocations(): Set<Vec3i> {
		this as MultiblockEntity
		return fluidInputOffsets.mapTo(mutableSetOf()) { (right, up, forward) -> getPosRelative(right, up, forward) }
	}

	/**
	 * Returns whether any of the internal storages can store the amount of the fluid provided
	 **/
	fun canStore(fluid: PipedFluid, amount: Int) = capacities.any { it.internalStorage.canStore(fluid, amount) }

	fun canStore(fluid: PipedFluid) = capacities.any { it.internalStorage.canStore(fluid, 0) }

	/**
	 * Returns the first internal storage that can contain the amount of the fluid provided.
	 **/
	fun firstCasStore(fluid: PipedFluid, amount: Int): StorageContainer? = capacities.firstOrNull { it.internalStorage.canStore(fluid, amount) }

	fun firstCasStore(fluid: PipedFluid): StorageContainer? = capacities.firstOrNull { it.internalStorage.canStore(fluid, 0) }

	fun getCapacityFor(fluid: PipedFluid): Int {
		return capacities.sumOf { if (it.internalStorage.canStore(fluid, 0)) it.internalStorage.remainingCapacity() else 0}
	}

	fun isFull(): Boolean = capacities.all { it.internalStorage.isFull() }

	fun isEmpty() = capacities.all { it.internalStorage.isEmpty() }

	fun canExtractAny(): Boolean = capacities.any { !it.internalStorage.isEmpty() }

	fun canStoreAny(): Boolean = capacities.any { !it.internalStorage.isFull() && it.internalStorage.inputAllowed }

	/**
	 * Adds the amount of the fluid to the first available internal storage
	 **/
	fun addFirstAvailable(fluid: PipedFluid, amount: Int): Int {
		var remaining = amount

		for (container in capacities.filter { it.internalStorage.getStoredFluid() == fluid || it.internalStorage.getStoredFluid() == null }) {
			val unfit = container.internalStorage.addAmount(fluid, remaining)
			remaining -= (remaining - unfit)

			if (remaining <= 0) break
		}

		return remaining
	}

	/**
	 * Removes the amount of the fluid to the first available internal storage
	 **/
	fun removeFirstAvailable(fluid: PipedFluid, amount: Int): Int {
		var remaining = amount

		for (container in capacities.filter { it.internalStorage.getStoredFluid() == fluid }) {
			val unRemoved = container.internalStorage.remove(fluid, remaining)
			remaining -= (remaining - unRemoved)

			if (remaining <= 0) break
		}

		return remaining
	}

	fun getStoredResources() : Map<PipedFluid?, Int> = capacities.associate { it.internalStorage.getStoredFluid() to it.internalStorage.getAmount() }

	fun getNamedStorage(name: String): StorageContainer = capacities.first { it.name == name }

	fun getStorage(key: NamespacedKey): StorageContainer = capacities.first { it.namespacedKey == key }

	fun storeFluidData(destination: PersistentMultiblockData, context: PersistentDataAdapterContext) {
		val storages = context.newPersistentDataContainer()
		capacities.forEach { it.save(storages) }
		destination.addAdditionalData(STORAGES, TAG_CONTAINER, storages)
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

	val fluidInputOffset: Vec3i

	fun getFluidInputLocation(): Vec3i {
		this as MultiblockEntity
		return getRelative(
			origin = vec3i,
			forwardFace= structureDirection,
			right = fluidInputOffset.x,
			up = fluidInputOffset.y,
			forward = fluidInputOffset.z
		)
	}
}
