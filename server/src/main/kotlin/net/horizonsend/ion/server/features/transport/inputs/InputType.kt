package net.horizonsend.ion.server.features.transport.inputs

sealed interface InputType {
	data object POWER : InputType
	data object FLUID : InputType
	data object E2 : InputType

	companion object {
		val types = arrayOf<InputType>(POWER, FLUID, E2)
		val byName = mapOf("POWER" to POWER, "FLUID" to FLUID, "E2" to E2)
		operator fun get(name: String) : InputType? = byName[name]
	}
}
