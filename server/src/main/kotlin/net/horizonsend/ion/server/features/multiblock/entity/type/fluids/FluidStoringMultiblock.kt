package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.inputs.InputsData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey

interface FluidStoringMultiblock : Iterable<FluidStorageContainer> {
	override fun iterator(): Iterator<FluidStorageContainer> {
		return getStores().iterator()
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

	fun canRemove(type: FluidType): Boolean {
		return getStores().any { container -> !container.getContents().isEmpty() && container.getContents().type == type }
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

	fun pushFluids() {
		this as MultiblockEntity
		val fluidGraph = manager.getTransportManager().fluidGraphs

		inputsData.inputs.forEach { t: InputsData.BuiltInputData ->
			// Global coordinate
			val inputLocation = toBlockKey(getPosRelative(t.offsetRight, t.offsetUp, t.offsetForward))
			fluidGraph.add(inputLocation)
			fluidGraph.graphPositions[inputLocation]?.depositToNetwork(inputLocation, this)
		}
	}

	fun getNamedStorage(name: String) = getStores().find { container -> container.name == name }
}
