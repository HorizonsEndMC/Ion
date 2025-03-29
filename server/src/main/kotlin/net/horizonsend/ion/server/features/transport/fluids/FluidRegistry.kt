package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.core.registries.Registry
import net.horizonsend.ion.server.core.registries.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registries.keys.FluidTypeKeys
import net.horizonsend.ion.server.core.registries.keys.KeyRegistry
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.horizonsend.ion.server.features.transport.fluids.types.SimpleFluid
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.WHITE

class FluidTypeRegistry() : Registry<FluidType>("FLUID_TYPE") {
	override val keySet: KeyRegistry<FluidType> = FluidTypeKeys

	override fun boostrap() {
		register(FluidTypeKeys.EMPTY, object : FluidType(FluidTypeKeys.EMPTY) {
			override val displayName: Component = text("Empty", WHITE)
			override val categories: Array<FluidCategory> = arrayOf()
		})

		register(FluidTypeKeys.HYDROGEN, GasFluid(FluidTypeKeys.HYDROGEN, AtmosphericGasKeys.HYDROGEN))
		register(FluidTypeKeys.NITROGEN, GasFluid(FluidTypeKeys.NITROGEN, AtmosphericGasKeys.NITROGEN))
		register(FluidTypeKeys.METHANE, GasFluid(FluidTypeKeys.METHANE, AtmosphericGasKeys.METHANE))
		register(FluidTypeKeys.OXYGEN, GasFluid(FluidTypeKeys.OXYGEN, AtmosphericGasKeys.OXYGEN))
		register(FluidTypeKeys.CHLORINE, GasFluid(FluidTypeKeys.CHLORINE, AtmosphericGasKeys.CHLORINE))
		register(FluidTypeKeys.FLUORINE, GasFluid(FluidTypeKeys.FLUORINE, AtmosphericGasKeys.FLUORINE))
		register(FluidTypeKeys.HELIUM, GasFluid(FluidTypeKeys.HELIUM, AtmosphericGasKeys.HELIUM))
		register(FluidTypeKeys.CARBON_DIOXIDE, GasFluid(FluidTypeKeys.CARBON_DIOXIDE, AtmosphericGasKeys.CARBON_DIOXIDE))

		register(FluidTypeKeys.WATER, SimpleFluid(FluidTypeKeys.WATER, text("Water", BLUE), arrayOf()))
	}
}
