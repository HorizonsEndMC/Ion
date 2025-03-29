package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.kyori.adventure.text.Component
import java.util.function.Supplier

abstract class GasOxidizer(
	identifier: String,
	displayName: Component,
	containerIdentifier: String,
	configurationSupplier: Supplier<Gasses.GasConfiguration>,
	transportedFluidSupplier: IonRegistryKey<FluidType, GasFluid>,

	val powerMultiplier: Double
) : Gas(identifier, displayName, containerIdentifier, configurationSupplier, transportedFluidSupplier)
