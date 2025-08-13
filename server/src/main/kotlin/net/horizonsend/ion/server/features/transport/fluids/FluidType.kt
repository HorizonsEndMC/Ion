package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.kyori.adventure.text.Component

abstract class FluidType(override val key: IonRegistryKey<FluidType, out FluidType>) : Keyed<FluidType> {
	abstract val displayName: Component
	abstract val categories: Array<FluidCategory>
}
