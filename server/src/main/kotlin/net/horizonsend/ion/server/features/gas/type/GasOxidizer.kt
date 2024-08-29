package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses
import net.horizonsend.ion.server.features.transport.fluids.types.GasPipedFluid
import net.kyori.adventure.text.Component
import java.util.function.Supplier

abstract class GasOxidizer(
	identifier: String,
	displayName: Component,
	containerIdentifier: String,
	configurationSupplier: Supplier<Gasses.GasConfiguration>,
	transportedFluidSupplier: Supplier<GasPipedFluid>,

	val powerMultiplier: Double
) : Gas(identifier, displayName, containerIdentifier, configurationSupplier, transportedFluidSupplier)
