package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.inputs.IOData.BuiltInputData
import net.horizonsend.ion.server.features.transport.inputs.IOPort.RegisteredMetaDataInput
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i

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

	fun getNamedStorage(name: String) = getStores().find { container -> container.name == name }

	fun bootstrapNetwork() {
		this as MultiblockEntity

		val fluidManager = manager.getTransportManager().getGraphTransportManager()

		for (portLocation: BuiltInputData<RegisteredMetaDataInput<FluidInputMetadata>> in ioData.getOfType(IOType.FLUID)) {
			val localPosition = toBlockKey(fluidManager.transportManager.getLocalCoordinate(toVec3i(portLocation.getRealPos(this))))
			if (portLocation.get(this)?.metaData?.outputAllowed != true) continue

			val network = fluidManager.getByLocation(localPosition)

			if (network != null) return

			fluidManager.registerNewPosition(localPosition)
		}
	}
}
