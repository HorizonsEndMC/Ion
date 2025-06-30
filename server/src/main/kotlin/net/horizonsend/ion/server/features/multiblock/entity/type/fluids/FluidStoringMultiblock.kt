package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType

interface FluidStoringMultiblock : Iterable<FluidStack> {
	override fun iterator(): Iterator<FluidStack> {
		return getStores().map { it -> it.getContents() }.iterator()
	}

	fun getStores(): List<FluidStorageContainer>

	fun saveStorageData(destination: PersistentMultiblockData) {
		getStores().forEach { store -> store.save(destination) }
	}

	fun canAdd(stack: FluidStack): Boolean {
		return getStores().any { container -> container.canAdd(stack) }
	}

	fun canRemove(stack: FluidStack): Boolean {
		return getStores().any { container -> container.canRemove(stack) }
	}

	fun contains(type: FluidType): Boolean {
		return getStores().any { container -> container.getContents().type == type }
	}

	fun getContaining(type: FluidType): List<FluidStorageContainer> {
		return getStores().filter { container -> container.getContents().type == type }
	}

	fun getRemovable(): List<FluidStorageContainer> {
		return getStores().filter { container -> !container.getContents().isEmpty() }
	}
}
