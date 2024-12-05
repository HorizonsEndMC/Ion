package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.InternalStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.horizonsend.ion.server.features.transport.fluids.Fluid
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.STORAGES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER

interface FluidStoringEntity {
	val fluidStores: Array<StorageContainer>

	/**
	 * Returns the first capacity, if one is available, that can store the fluid stack
	 **/
	fun firstCapacityCanStore(fluid: FluidStack): StorageContainer? {
		return fluidStores.firstOrNull {
			it.internalStorage.inputAllowed && it.internalStorage.canStore(fluid)
		}
	}

	/**
	 * Returns whether any capacity can store the fluid stack
	 **/
	fun anyCapacityCanStore(fluid: FluidStack): Boolean = firstCapacityCanStore(fluid) != null

	/**
	 * Returns the first capacity, if one is available, that can store the fluid type
	 **/
	fun firstCapacityCanStore(type: Fluid): StorageContainer? {
		return fluidStores.firstOrNull {
			it.internalStorage.inputAllowed && it.internalStorage.canStore(type)
		}
	}

	/**
	 * Returns whether any capacity can store the fluid type
	 **/
	fun anyCapacityCanStore(type: Fluid): Boolean = firstCapacityCanStore(type) != null

	fun getCapacityFor(fluid: Fluid): Int {
		return fluidStores.sumOf {
			if (it.internalStorage.canStore(fluid)) it.internalStorage.remainingCapacity() else 0
		}
	}

	fun isFull(): Boolean = fluidStores.all { it.internalStorage.isFull() }

	fun isEmpty() = fluidStores.all { it.internalStorage.isEmpty() }

	fun canExtractAny(): Boolean = fluidStores.any { !it.internalStorage.isEmpty() }

	fun canStoreAny(): Boolean = fluidStores.any { !it.internalStorage.isFull() && it.internalStorage.inputAllowed }

	/**
	 * Adds the amount of the fluid to the first available internal storage
	 **/
	fun addFirstAvailable(fluid: FluidStack): Int {
		var remaining = fluid.amount

		for (container in fluidStores.filter { it.internalStorage.canStore(fluid.type) }) {
			container.internalStorage.setFluid(fluid.type)
			val remainder = container.internalStorage.addAmount(remaining)
			remaining -= (remaining - remainder)

			if (remaining <= 0) break
		}

		return remaining
	}

	/**
	 * Removes the amount of the fluid to the first available internal storage
	 **/
	fun removeFirstAvailable(fluid: Fluid, amount: Int): Int {
		var remaining = amount

		for (container in fluidStores.filter { it.internalStorage.getFluidType() == fluid }) {
			val unRemoved = container.internalStorage.removeAmount(remaining)
			remaining -= (remaining - unRemoved)

			if (remaining <= 0) break
		}

		return remaining
	}

	fun getStoredResources(): Map<Fluid?, Int> = fluidStores.associate { it.internalStorage.getFluidType() to it.internalStorage.getAmount() }

	fun getExtractableResources(): Map<InternalStorage, Pair<Fluid, Int>> {
		val entries = mutableMapOf<InternalStorage, Pair<Fluid, Int>>()

		for (capacity in fluidStores) {
			val fluid = capacity.internalStorage.getFluidType() ?: continue
			if (!capacity.internalStorage.extractionAllowed) continue
			val amount = capacity.internalStorage.getAmount()
			if (amount <= 0) continue

			entries[capacity.internalStorage] = fluid to capacity.internalStorage.getAmount()
		}

		return entries
	}

	fun getNamedStorage(name: String): StorageContainer = fluidStores.first { it.name == name }

	fun getStorage(key: NamespacedKey): StorageContainer = fluidStores.first { it.namespacedKey == key }

	fun storeFluidData(destination: PersistentMultiblockData, context: PersistentDataAdapterContext) {
		val storages = context.newPersistentDataContainer()
		fluidStores.forEach { it.save(storages) }
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
