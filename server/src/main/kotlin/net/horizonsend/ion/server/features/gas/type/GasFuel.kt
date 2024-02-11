package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses
import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor
import net.horizonsend.ion.server.features.world.IonWorld
import net.kyori.adventure.text.Component
import java.util.function.Supplier

abstract class GasFuel(
	identifier: String,
	displayName: Component,
	containerIdentifier: String,
	configurationSupplier: Supplier<Gasses.GasConfiguration>,
	collectionFactorSupplier: (IonWorld) -> List<CollectionFactor>,

	val powerPerUnit: Int,
	val cooldown: Int
) : Gas(identifier, displayName, containerIdentifier, configurationSupplier, collectionFactorSupplier)

