@file:Suppress("UNCHECKED_CAST")

package net.horizonsend.ion.server.features.transport.inputs

import net.horizonsend.ion.server.features.multiblock.entity.type.e2.E2PortMetaData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.transport.inputs.IOPort.RegisteredMetaDataInput
import net.horizonsend.ion.server.features.transport.inputs.IOPort.Simple
import kotlin.reflect.KClass

sealed class IOType<T : IOPort>(val displayName: String, val clazz: KClass<T>) {
	data object POWER : IOType<Simple>("POWER", Simple::class)
	data object FLUID : IOType<RegisteredMetaDataInput<FluidInputMetadata>>("FLUID", RegisteredMetaDataInput::class as KClass<RegisteredMetaDataInput<FluidInputMetadata>>)
	data object E2 : IOType<RegisteredMetaDataInput<E2PortMetaData>>("E2", RegisteredMetaDataInput::class as KClass<RegisteredMetaDataInput<E2PortMetaData>>)

	companion object {
		val types = arrayOf<IOType<*>>(POWER, FLUID, E2)
		val byName get() = mapOf("POWER" to POWER, "FLUID" to FLUID, "E2" to E2)
		operator fun get(name: String) : IOType<*>? = byName[name]
	}
}
