package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.transport.inputs.IOPort.InputMetaData

class FluidInputMetadata(
	val connectedStore: FluidStorageContainer,

	val inputAllowed: Boolean,
	outputAllowed: Boolean,
	private val outputCondition: () -> Boolean = { true }
) : InputMetaData {
	private val outputConfigured = outputAllowed

	val outputAllowed: Boolean
		get() = outputConfigured && outputCondition()
}
