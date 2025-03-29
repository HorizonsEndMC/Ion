package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.kyori.adventure.text.Component
import java.util.function.Supplier

open class GasFuel(
	identifier: String,
	displayName: Component,
	containerIdentifier: IonRegistryKey<CustomItem, GasCanister>,
	configurationSupplier: Supplier<Gasses.GasConfiguration>,
	transportedFluidSupplier: IonRegistryKey<FluidType, GasFluid>,

	val powerPerUnit: Int,
	val cooldown: Int
) : Gas(identifier, displayName, containerIdentifier, configurationSupplier, transportedFluidSupplier)

