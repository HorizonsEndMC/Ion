package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.kyori.adventure.text.Component

class SimpleFluid(key: IonRegistryKey<FluidType, out FluidType>, override val displayName: Component, override val categories: Array<FluidCategory>) : FluidType(key)
