package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.core.registries.Registry
import net.horizonsend.ion.server.core.registries.keys.FluidTypeKeys
import net.horizonsend.ion.server.core.registries.keys.KeyRegistry
import net.horizonsend.ion.server.features.gas.Gasses
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

		register(FluidTypeKeys.HYDROGEN, GasFluid(FluidTypeKeys.HYDROGEN) { Gasses.HYDROGEN })
		register(FluidTypeKeys.NITROGEN, GasFluid(FluidTypeKeys.NITROGEN) { Gasses.NITROGEN })
		register(FluidTypeKeys.METHANE, GasFluid(FluidTypeKeys.METHANE) { Gasses.METHANE })
		register(FluidTypeKeys.OXYGEN, GasFluid(FluidTypeKeys.OXYGEN) { Gasses.OXYGEN })
		register(FluidTypeKeys.CHLORINE, GasFluid(FluidTypeKeys.CHLORINE) { Gasses.CHLORINE })
		register(FluidTypeKeys.FLUORINE, GasFluid(FluidTypeKeys.FLUORINE) { Gasses.FLUORINE })
		register(FluidTypeKeys.HELIUM, GasFluid(FluidTypeKeys.HELIUM) { Gasses.HELIUM })
		register(FluidTypeKeys.CARBON_DIOXIDE, GasFluid(FluidTypeKeys.CARBON_DIOXIDE) { Gasses.CARBON_DIOXIDE })

		register(FluidTypeKeys.WATER, SimpleFluid(FluidTypeKeys.WATER, text("Water", BLUE), arrayOf()))
	}
}
