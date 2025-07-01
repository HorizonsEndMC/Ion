package net.horizonsend.ion.server.features.transport.inputs

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.transport.inputs.RegisteredInput.RegisteredMetaDataInput
import net.horizonsend.ion.server.features.transport.inputs.RegisteredInput.Simple
import kotlin.reflect.KClass

sealed class InputType<T : RegisteredInput>(val clazz: KClass<T>) {
	data object POWER : InputType<Simple>(Simple::class)
	data object FLUID : InputType<RegisteredMetaDataInput<FluidInputMetadata>>(RegisteredMetaDataInput::class as KClass<RegisteredMetaDataInput<FluidInputMetadata>>)
	data object E2 : InputType<Simple>(Simple::class)

	companion object {
		val types = arrayOf<InputType<*>>(POWER, FLUID, E2)
		val byName = mapOf("POWER" to POWER, "FLUID" to FLUID, "E2" to E2)
		operator fun get(name: String) : InputType<*>? = byName[name]
	}
}
