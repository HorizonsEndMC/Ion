package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.features.transport.fluids.Fluid
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.kyori.adventure.text.Component

class SimpleFluid(identifier: String, override val displayName: Component, override val categories: Array<FluidCategory>) : Fluid(identifier)
