package net.horizonsend.ion.server.features.industry

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidType

enum class FluidFuelProperties(
	val fluidType: IonRegistryKey<FluidType, out FluidType>,
	val joulesPerLiter: Double
) {
	HYDROGEN(FluidTypeKeys.HYDROGEN, 200_000.0),
	NITROGEN(FluidTypeKeys.NITROGEN, 1_000_000.0),
	METHANE(FluidTypeKeys.METHANE, 3_000_000.0),
	LAVA(FluidTypeKeys.LAVA, 6_000_000.0);

	companion object {
		private val byFluidType: Map<IonRegistryKey<FluidType, out FluidType>, FluidFuelProperties> =
			entries.associateBy(FluidFuelProperties::fluidType)

		val acceptedFluidTypes: Set<IonRegistryKey<FluidType, out FluidType>> = byFluidType.keys

		operator fun get(fluidType: IonRegistryKey<FluidType, out FluidType>): FluidFuelProperties? =
			byFluidType[fluidType]
	}
}
