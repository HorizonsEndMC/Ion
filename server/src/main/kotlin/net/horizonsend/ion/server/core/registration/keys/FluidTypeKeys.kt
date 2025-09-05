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
}
