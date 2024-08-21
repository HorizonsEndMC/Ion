package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses
import net.kyori.adventure.text.Component
import java.util.function.Supplier

abstract class GasOxidizer(
	identifier: String,
	displayName: Component,
	containerIdentifier: String,
	configurationSupplier: Supplier<Gasses.GasConfiguration>,

	val powerMultiplier: Double
) : Gas(identifier, displayName, containerIdentifier, configurationSupplier)
