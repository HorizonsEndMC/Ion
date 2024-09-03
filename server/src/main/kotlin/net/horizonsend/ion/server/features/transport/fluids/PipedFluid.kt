package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.kyori.adventure.text.Component

abstract class PipedFluid(val identifier: String) {
	abstract val displayName: Component
	abstract val categories: Array<FluidCategory>

	override fun toString(): String {
		return "FLUID[$identifier]"
	}
}
