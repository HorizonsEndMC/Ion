package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.InternalStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.grid.FluidGrid
import net.horizonsend.ion.server.features.transport.grid.util.Sink
import net.horizonsend.ion.server.features.transport.grid.util.Source
import net.horizonsend.ion.server.features.transport.node.fluid.FluidInputNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.STORAGES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER

interface FluidStoringEntity : Source, Sink {
	val capacities: Array<StorageContainer>

	/**
	 * Returns whether any of the internal storages can store the amount of the fluid provided
	 **/
	fun canStore(fluid: PipedFluid, amount: Int) = capacities.any { it.storage.canStore(fluid, amount) }

	/**
	 * Returns the first internal storage that can contain the amount of the fluid provided.
	 **/
	fun firstCasStore(fluid: PipedFluid, amount: Int): StorageContainer? = capacities.firstOrNull { it.storage.canStore(fluid, amount) }

	/**
	 * Adds the amount of the fluid to the first available internal storage
	 **/
	fun addFirstAvailable(fluid: PipedFluid, amount: Int): Int {
		var remaining = amount

		for (container in capacities.filter { it.storage.getStoredFluid() == fluid || it.storage.getStoredFluid() == null }) {
			val unfit = container.storage.addAmount(fluid, remaining)
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

		for (container in capacities.filter { it.storage.getStoredFluid() == fluid }) {
			val unRemoved = container.storage.remove(fluid, remaining)
			remaining -= (remaining - unRemoved)

			if (remaining <= 0) break
		}

		return remaining
	}

	fun getStoredResources() : Map<PipedFluid?, Int> = capacities.associate { it.storage.getStoredFluid() to it.storage.getAmount() }

	fun getNamedStorage(name: String): StorageContainer = capacities.first { it.name == name }

	fun getStorage(key: NamespacedKey): StorageContainer = capacities.first { it.namespacedKey == key }

	fun storeFluidData(destination: PersistentDataContainer, context: PersistentDataAdapterContext) {
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

	fun getFluidInputNode(): FluidInputNode? {
		this as MultiblockEntity
		val block = getFluidInputLocation()

		val chunk = IonChunk[world, block.x.shr(4), block.z.shr(4)] ?: return null
		val manager = chunk.transportNetwork.powerNodeManager
		val node = manager.getInternalNode(toBlockKey(block))

		if (node != null) return node as? FluidInputNode

		// Try to place unregistered node
		manager.manager.processBlockAddition(world.getBlockAt(block.x, block.y, block.z))
		return manager.getInternalNode(toBlockKey(block)) as? FluidInputNode
	}

	fun bindFluidInput() {
		val existing = getFluidInputNode() ?: return
		if (existing.boundMultiblockEntity != null) return

		existing.boundMultiblockEntity = this
	}

	fun releaseFluidInput() {
		val existing = getFluidInputNode() ?: return
		if (existing.boundMultiblockEntity != this) return

		existing.boundMultiblockEntity = null
	}

	/**
	 * Returns the grid that this multiblock is tied to
	 *
	 * Should only return null if the multiblock is partially unloaded, or not intact.
	 **/
	fun getFluidGrid(): FluidGrid? {
		return getFluidInputNode()?.grid as? FluidGrid
	}

	override fun isProviding(): Boolean {
		return getStoredResources().entries.any { it.key != null && it.value > 0 }
	}

	override fun isRequesting(): Boolean {
		return capacities.any { it.storage.getAmount() < it.storage.getCapacity() }
	}
}
