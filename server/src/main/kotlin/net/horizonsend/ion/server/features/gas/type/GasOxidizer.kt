package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses.GasConfiguration
import net.kyori.adventure.text.Component
import java.util.function.Supplier

abstract class GasOxidizer(
	identifier: String,
	displayName: Component,
	containerIdentifier: String,
	factorSupplier: Supplier<GasConfiguration>,

	val powerMultipler: Double
) : Gas(identifier, displayName, containerIdentifier, factorSupplier)

