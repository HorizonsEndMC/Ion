package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.transport.inputs.RegisteredInput.InputMetaData

class FluidInputMetadata(
	val connectedStore: FluidStorageContainer,

	val inputAllowed: Boolean,
	val outputAllowed: Boolean
) : InputMetaData {
}
