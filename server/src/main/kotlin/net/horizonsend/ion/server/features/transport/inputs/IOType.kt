@file:Suppress("UNCHECKED_CAST")

package net.horizonsend.ion.server.features.transport.inputs

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidPortMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyPortMetaData
import net.horizonsend.ion.server.features.transport.inputs.IOPort.RegisteredMetaDataInput
import net.horizonsend.ion.server.features.transport.inputs.IOPort.Simple
import kotlin.reflect.KClass

sealed class IOType<T : IOPort>(val displayName: String, val clazz: KClass<T>) {
	data object POWER : IOType<Simple>("POWER", Simple::class)
	data object FLUID : IOType<RegisteredMetaDataInput<FluidPortMetadata>>("FLUID", RegisteredMetaDataInput::class as KClass<RegisteredMetaDataInput<FluidPortMetadata>>)
	data object GRID_ENERGY : IOType<RegisteredMetaDataInput<GridEnergyPortMetaData>>("GRID_ENERGY", RegisteredMetaDataInput::class as KClass<RegisteredMetaDataInput<GridEnergyPortMetaData>>)

	companion object {
		val types = arrayOf<IOType<*>>(POWER, FLUID, GRID_ENERGY)
		val byName get() = mapOf("POWER" to POWER, "FLUID" to FLUID, "GRID_ENERGY" to GRID_ENERGY)
		operator fun get(name: String) : IOType<*>? = byName[name]
	}
}
