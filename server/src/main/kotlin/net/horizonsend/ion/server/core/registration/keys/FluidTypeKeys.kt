package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid

object FluidTypeKeys : KeyRegistry<FluidType>(RegistryKeys.FLUID_TYPE, FluidType::class) {
	val EMPTY = registerKey("EMPTY")

	val HYDROGEN = registerTypedKey<GasFluid>("HYDROGEN")
	val NITROGEN = registerTypedKey<GasFluid>("NITROGEN")
	val METHANE = registerTypedKey<GasFluid>("METHANE")
	val OXYGEN = registerTypedKey<GasFluid>("OXYGEN")
	val CHLORINE = registerTypedKey<GasFluid>("CHLORINE")
	val FLUORINE = registerTypedKey<GasFluid>("FLUORINE")
	val HELIUM = registerTypedKey<GasFluid>("HELIUM")
	val CARBON_DIOXIDE = registerTypedKey<GasFluid>("CARBON_DIOXIDE")

	val WATER = registerKey("WATER")
	val LAVA = registerKey("LAVA")

	val LOW_PRESSURE_STEAM = registerKey("LOW_PRESSURE_STEAM")
	val DENSE_STEAM = registerKey("DENSE_STEAM")
	val SUPER_DENSE_STEAM = registerKey("SUPER_DENSE_STEAM")
	val ULTRA_DENSE_STEAM = registerKey("ULTRA_DENSE_STEAM")

	val CRUDE_OIL = registerKey("CRUDE_OIL")

	val POLLUTION = registerKey("POLLUTION")
//	val HAZARDOUS_POLLUTION = registerKey("HAZARDOUS_POLLUTION")
//	val DANGEROUS_POLLUTION = registerKey("DANGEROUS_POLLUTION")

//	val PETROLEUM_GAS = registerKey("PETROLEUM_GAS")
//	val LIGHT_OIL = registerKey("LIGHT_OIL")
//	val NAPTHA = registerKey("NAPTHA")
//	val HEAVY_OIL = registerKey("HEAVY_OIL")
}
